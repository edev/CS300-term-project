package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.gui.ChatScreen;
import com.dylanlaufenberg.portlandstate.cs300.gui.LoginScreen;
import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.Channel;

/**
 * Controller class for the client application. Handles the business logic and manages the GUI states.
 */
public class ClientController {

    private static ChatClient client;
    private static Channel channel;
    public static String userName;
    public static ChatScreen chatScreen;
    public static LoginScreen loginScreen;

    public static void main(String[] args) throws Exception {
        loginScreen = LoginScreen.createAndShow();
    }

    public static void login(String hostname, int port, String userName, String password) {
        if(loginOrRegister(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_LOGIN, hostname, port, userName, password)) {
            // Login request sent.
        } else {
            // Login request was not sent.
        }
    }

    public static void register(String hostname, int port, String userName, String password) {
        if(loginOrRegister(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_REGISTER, hostname, port, userName, password)) {
            // Register request sent.
        } else {
            // Register request was not sent.
        }
    }


    /**
     * Perform either a login or register action. Forms a Protobuf Message, stops the current client (if any),
     * connects to a new client, and writes the Message to the server.
     * @param messageType Either MessageType.LOGIN or MessageType.REGISTER
     * @param hostname The name of the server in any format Netty will accept
     * @param port The port of the server, between 1 and 65535, inclusive.
     * @param userName User name to be sent to the server to authenticate.
     * @param password Password to be sent to the server to authenticate.
     * @return True if the message was sent, false otherwise. (DOES NOT indicate a successful login.)
     */
    private static boolean loginOrRegister(NetMessage.Message.AuthenticationMessage.AuthMessageType messageType,
                                           String hostname,
                                           int port,
                                           String userName,
                                           String password) {
        if(messageType == null
                || (messageType != NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_LOGIN
                    && messageType != NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_REGISTER)
                || hostname == null
                || hostname.length() == 0
                || port <= 0
                || userName == null
                || userName.length() == 0
                || password == null
                || password.length() == 0) {
            // Error detected.
            System.err.println("Called ClientController.login(...) with invalid arguments. Ignoring.");
        } else {
            NetMessage.Message message = NetMessage.Message.newBuilder()
                    .setAuthMessage(
                            NetMessage.Message.AuthenticationMessage.newBuilder()
                                    .setAuthMessageType(messageType)
                                    .setUserName(userName)
                                    .setPassword(password)
                                    .build())
                    .build();

            if(client != null) {
                client.stop();
            }

            client = new ChatClient();
            client.host = hostname;
            client.port = port;
            channel = client.run(); // Synchronous. // TODO Make this asynchronous, using a future.

            if(channel != null) {
                // Successfully connected!
                System.out.println("Successfully connected! Sending message:");
                System.out.println(message.toString());
                System.out.println();
                channel.writeAndFlush(message);
                return true;
            }
        }
        return false;
    }

    public static void goOnline() {
        if(loginScreen != null) {
            loginScreen.close();
            loginScreen = null;
            chatScreen = ChatScreen.createAndShow();
        } // Else we're already online.
    }

    public static void goOffline() {
        // TODO Implement. (Reverse of goOnline.)
    }
}
