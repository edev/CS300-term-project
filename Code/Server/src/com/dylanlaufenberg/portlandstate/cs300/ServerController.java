package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Controller for the server program. Receives and routes incoming requests from ChatConnections.
 */
class ServerController {
    public static SortedMap<String, User> users = Collections.synchronizedSortedMap((new TreeMap<String, User>()));
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * Processes an incoming message from a channel.
     * @param user The user to whom the channel belongs (null if no user yet)
     * @param message The NetMessage.Message object to be processed
     * @param channel The channel on which the message arrived
     * @return The user associated with the channel, which may have changed during the transaction
     */
    public static User process(User user, NetMessage.Message message, Channel channel) {
        System.out.println("Received message: ");
        System.out.println(message.toString());

        // See which type of message is actually present.
        switch(message.getMessageContentsCase()) {
            case AUTHMESSAGE:
                user = processAuthMessage(message.getAuthMessage(), channel);
                break;

            case NOTICEMESSAGE:
                // Do nothing. Why is a client telling the server something with a notice message?!
                System.out.println("Ignoring received notice:");
                System.out.println(message.getNoticeMessage().toString());
                break;

            case CHATMESSAGE:
                if(!processChatMessage(user, message.getChatMessage(), channel)) {
                    // message not successfully delivered.
                    SharedHelper.error("Could not process message from " + user.name + ": " + message.toString());
                }
                break;

            default:
                SharedHelper.error("Invalid incoming message from " + user.name + ": " + message.toString());
                break;
        }

        return user;
    }

    private static User processAuthMessage(NetMessage.Message.AuthenticationMessage message, Channel channel) {
        if(message == null) {
            SharedHelper.error("processAuthMessage received a null message on the channel:.", channel);
            return null;
        }

        if(channel == null) {
            SharedHelper.error("processAuthMessage received the following message on a null channel:", message);
            return null;
        }

        switch(message.getAuthMessageType()) {
            case AUTH_REGISTER:
                return register(message.getUserName(), message.getPassword(), channel);

            case AUTH_LOGIN:
                return login(message.getUserName(), message.getPassword(), channel);

            default:
                System.out.println("Unrecognized message type. (This may be an unimplemented feature.)");
                return null;
        }
    }

    private static User login(String userName, String password, Channel channel) {
        if(userName == null
                || userName.length() == 0) {
            SharedHelper.error("login called with no userName.");
            return null;
        }

        if(password == null
                || password.length() == 0) {
            SharedHelper.error("login called for userName " + userName + " with no password.");
            return null;
        }

        if(channel == null) {
            SharedHelper.error("login called for userName " + userName + " with no channel.");
            return null;
        }

        User.AuthResult result = User.loadUser(userName, password);
        return processAuthResult(result, channel);
    }

    private static User register(String userName, String password, Channel channel) {
        if(userName == null
                || userName.length() == 0) {
            SharedHelper.error("register called with no userName.");
            return null;
        }

        if(password == null
                || password.length() == 0) {
            SharedHelper.error("register called for userName " + userName + " with no password.");
            return null;
        }

        if(channel == null) {
            SharedHelper.error("register called for userName " + userName + " with no channel.");
            return null;
        }

        User.AuthResult result = User.newUser(userName, password);
        return processAuthResult(result, channel);
    }

    private static User processAuthResult(User.AuthResult result, Channel channel) {
        if(result == null) {
            SharedHelper.error("processAuthResult called with no result.");
            return null;
        }

        if(channel == null) {
            SharedHelper.error("processAuthResult called with no channel, with result:", result);
            return null;
        }

        if (result.result == User.AuthResult.Result.BAD_USER) {

            // Respond no to the registration request. Don't sever the connection - it will be closed by the channel handler.
            channel.writeAndFlush(
                    ServerHelper.buildAuthResponseMessage(
                            NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_ERROR_USER
                    )
            );
            return null;

        } else if (result.result == User.AuthResult.Result.BAD_PASSWORD) {

            // Respond no to the registration request. Don't sever the connection - it will be closed by the channel handler.
            channel.writeAndFlush(
                    ServerHelper.buildAuthResponseMessage(
                            NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_ERROR_PASSWORD
                    )
            );
            return null;
        } else if (result.result == User.AuthResult.Result.SUCCESS && result.user != null) {

            // Success! Configure the user's channels.
            User newUser = result.user;
            newUser.channel = channel;
            newUser.broadcast = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            newUser.broadcast.addAll(channels);

            // Write login notification message to online users
            channels.writeAndFlush(
                    ServerHelper.buildNoticeMessage(
                            NetMessage.Message.NoticeMessage.NoticeMessageType.ONLINE,
                            result.user.name
                    )
            );

            // Add new user to other users' broadcasts and to the user collection, and add channel to our channel group.
            users.forEach(
                    (name, user) -> user.broadcast.add(channel)
            );

            // Build this before adding newUser to users.
            NetMessage.Message userList = ServerHelper.buildUserListMessage(users);
            users.putIfAbsent(newUser.name, newUser);
            channels.add(channel);

            // Respond affirmatively to registration request. Then immediately send the user list.
            channel.write(
                    ServerHelper.buildAuthResponseMessage(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_SUCCESS));
            channel.writeAndFlush(
                    userList);

            return newUser;
        } else {

            // We've exhausted all valid cases, so if this code executes, we must have an invalid result object!
            SharedHelper.error("processAuthResult received an invalid result!", result);
            return null;
        }
    }



    private static boolean processChatMessage(User user, NetMessage.Message.ChatMessage message, Channel channel) {
        if(user == null
                || message == null
                || channel == null) {
            return false;
        }

        // Dispatch appropriately and pass the return value through.
        switch(message.getChatMessageType()) {
            case PUBLIC:
                return publicMessage(user, message.getText());

            case PRIVATE:
                return privateMessage(user, message.getReceiver(), message.getText());

            default:
                return false;
        }
    }

    private static boolean publicMessage(User sender, String text) {
        if(sender == null
                || text == null
                || text.equals("")
                || sender.broadcast == null) {
            return false;
        }

        // Send the message to everyone else, with the sender marked.
        sender.broadcast.writeAndFlush(
                ServerHelper.buildChatResponseMessage(
                        NetMessage.Message.ChatMessage.ChatMessageType.PUBLIC,
                        sender.name,
                        text
                )
        );

        return true;
    }

    private static boolean privateMessage(User sender, String receiverName, String text) {
        if(sender == null
                || receiverName == null
                || receiverName.trim().equals("")
                || text == null
                || text.trim().equals("")) {
            SharedHelper.error("Ignoring malformed private message:");
            return false;
        }

        User receiver = users.get(receiverName);
        if(receiver == null
                || receiver.channel == null) {
            SharedHelper.error("Ignoring private message directed toward invalid or offline user.");
            return false;
        }

        // Send the message to the receiver, with the sender marked.
        receiver.channel.writeAndFlush(
                ServerHelper.buildChatResponseMessage(
                        NetMessage.Message.ChatMessage.ChatMessageType.PRIVATE,
                        sender.name,
                        text
                )
        );

        return true;
    }

}
