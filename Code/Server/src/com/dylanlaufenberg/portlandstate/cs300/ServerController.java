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

        switch(message.getMessageType()) {
            case REGISTER:
                // TODO Replace registration stub with real code.
                if(message.hasCredentialData()) {
                    user = register(message.getCredentialData().getUserName(),
                            message.getCredentialData().getPassword(),
                            channel);
                }

                break;

            case LOGIN:
                // TODO Allow login.
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

        User newUser = User.newUser(userName, password);
        if(newUser == null) {
            // Respond no to the registration request. Don't sever the connection - it will be closed by the channel handler.
            channel.writeAndFlush(
                    NetMessage.Message.newBuilder()
                            .setMessageType(NetMessage.Message.MessageType.REGISTER)
                            .setCredentialData(
                                    NetMessage.Message.Credentials.newBuilder()
                                            .setLoggedIn(false)
                                            .build()
                            )
            );

            return null;
        } // Else created a new User.
        newUser.channel = channel;
        newUser.broadcast = channels;

        // Write login notification message to online users
        channels.writeAndFlush(
                NetMessage.Message.newBuilder()
                        .setMessageType(NetMessage.Message.MessageType.NOTICE)
                        .setMessage(userName + " is now online.")
                        .build()
        );

        // Add new user to other users' broadcasts and to the user collection, and add channel to our channel group.
        users.forEach((name, user)->user.broadcast.add(channel));
        users.putIfAbsent(userName, newUser);
        channels.add(channel);

        // Respond to registration request.
        channel.writeAndFlush(
                NetMessage.Message.newBuilder()
                        .setMessageType(NetMessage.Message.MessageType.REGISTER)
                        .setCredentialData(
                                NetMessage.Message.Credentials.newBuilder()
                                        .setLoggedIn(true)
                                        .build()
                        )
        );
        return newUser;
    }
}
