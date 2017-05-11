package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.gui.LoginScreen;
import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.Channel;

/**
 * Controller class for the client application. Handles the business logic and manages the GUI states.
 */
public class ChatApplication {

    private static ChatClient client;
    private static Channel channel;

    public static void main(String[] args) throws Exception {
        LoginScreen.createAndShow();
    }

    public static void login(String hostname, int port, String userName, String password) {

        if(hostname == null || hostname.length() == 0 || port <= 0 || userName == null || userName.length() == 0
                || password == null || password.length() == 0) {
            // Error detected.
            System.err.println("Called ChatApplication.login(...) with invalid arguments. Ignoring.");
        } else {

            NetMessage.Message message = NetMessage.Message.newBuilder()
                    .setCredentialData(
                            NetMessage.Message.Credentials.newBuilder()
                                    .setUserName(userName)
                                    .setPassword(password)
                                    .build())
                    .setMessageType(NetMessage.Message.MessageType.LOGIN)
                    .build();

            if(client != null) {
                client.stop();
            }

            client = new ChatClient();
            client.host = hostname;
            client.port = port;
            channel = client.run(); // Synchronous.

            if(channel != null) {
                // Successfully connected!
                System.out.println("Successfully connected! Sending message:");
                System.out.println(message.toString());
                channel.writeAndFlush(message);

            }
        }
    }
}
