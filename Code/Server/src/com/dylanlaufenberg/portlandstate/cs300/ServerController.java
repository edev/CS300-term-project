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

    public static User process(User user, NetMessage.Message message, Channel channel) {
        // TODO Do something useful
        System.out.println("Received message: ");
        System.out.println(message.toString());

        // See which type of message is actually present.
        switch(message.getMessageContentsCase()) {
            case AUTHMESSAGE:
                user = processAuthMessage(user, message.getAuthMessage(), channel);
                break;

                // TODO Handle other cases.
            // TODO Handle unset case.
        }

        return user;
    }

    private static User processAuthMessage(User user, NetMessage.Message.AuthenticationMessage message, Channel channel) {
        switch(message.getAuthMessageType()) {
            case AUTH_REGISTER:
                // TODO Replace registration stub with real code.
                user = register(message.getUserName(), message.getPassword(), channel);
                break;

            case AUTH_LOGIN:
                // Temporary response: user doesn't exist.
                channel.writeAndFlush(
                        NetMessage.Message.newBuilder()
                                .setAuthMessage(
                                        NetMessage.Message.AuthenticationMessage.newBuilder()
                                                .setAuthMessageType(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_ERROR_USER)
                                                .build()
                                )
                                .build()
                );
                // TODO Allow login. Remove temporary response above.
                break;

            default:
                System.out.println("Unrecognized message type. (This may be an unimplemented feature.)");
                break;
        }
        return user;
    }

    private static User register(String userName, String password, Channel channel) {
        if(userName == null
                || userName.length() == 0
                || password == null
                || password.length() == 0
                || channel == null) {
            // Error detected. We can't proceed. No new user will be activated.
            return null;
        }

        User.AuthResult result = User.newUser(userName, password);
        if(result.result == User.AuthResult.Result.BAD_USER) {

            // Respond no to the registration request. Don't sever the connection - it will be closed by the channel handler.
            channel.writeAndFlush(
                    NetMessage.Message.newBuilder()
                            .setAuthMessage(
                                    NetMessage.Message.AuthenticationMessage.newBuilder()
                                        .setAuthMessageType(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_ERROR_USER)
                                        .build()
                            )
                    .build()
            );

        } else if(result.result == User.AuthResult.Result.BAD_PASSWORD) {

            // Respond no to the registration request. Don't sever the connection - it will be closed by the channel handler.
            channel.writeAndFlush(
                    NetMessage.Message.newBuilder()
                            .setAuthMessage(
                                    NetMessage.Message.AuthenticationMessage.newBuilder()
                                            .setAuthMessageType(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_ERROR_PASSWORD)
                                            .build()
                            )
                            .build()
            );

        } else if(result.result == User.AuthResult.Result.SUCCESS && result.user != null) {

            // Success!
            User newUser = result.user;

            newUser.channel = channel;
            newUser.broadcast = channels;

            // Write login notification message to online users
            channels.writeAndFlush(
                    NetMessage.Message.newBuilder()
                            .setNoticeMessage(
                                    NetMessage.Message.NoticeMessage.newBuilder()
                                            .setNoticeMessageType(NetMessage.Message.NoticeMessage.NoticeMessageType.ONLINE)
                                            .setUserName(userName)
                                            .build()
                            )
                            .build()
            );

            // Add new user to other users' broadcasts and to the user collection, and add channel to our channel group.
            users.forEach((name, user) -> user.broadcast.add(channel));
            users.putIfAbsent(userName, newUser);
            channels.add(channel);

            // Respond affirmatively to registration request.
            channels.writeAndFlush(
                    NetMessage.Message.newBuilder()
                            .setAuthMessage(
                                    NetMessage.Message.AuthenticationMessage.newBuilder()
                                            .setAuthMessageType(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_SUCCESS)
                                            .build()
                            )
                            .build()
            );

            return newUser;

        } else {
            // What the hell? Invalid result object!
        }

        // We haven't yet found and returned a user, so our result defaults to null.
        return null;
    }
}
