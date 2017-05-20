package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.gui.ChatScreen;
import com.dylanlaufenberg.portlandstate.cs300.gui.LoginScreen;
import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.Channel;

/**
 * Controller class for the client application. Handles the business logic and manages the GUI states.
 */
public class ClientController {

    private ChatClient client;
    private Channel channel;
    public String userName;
    public ChatScreen chatScreen;
    public LoginScreen loginScreen;

    public static void main(String[] args) throws Exception {
        // FIXME Try making ClientController non-static so the object can be passed between classes.
        ClientController c = new ClientController();
        c.loginScreen = LoginScreen.createAndShow(c); // FIXME Refactor and reorganize appropriately
        c.chatScreen = new ChatScreen(c);
        c.loginScreen.show();
    }

    public void processMessage(NetMessage.Message m) {
        switch(m.getMessageContentsCase()) {
            case AUTHMESSAGE:
                processAuthMessage(m.getAuthMessage());
                break;

            case NOTICEMESSAGE:
                // processNotice(m); // TODO Fix processNotice
                break;

            case CHATMESSAGE:
                processChatMessage(m.getChatMessage());
                break;
        }
    }

    private void processAuthMessage(NetMessage.Message.AuthenticationMessage m) {
        if(m == null) {
            return;
        }

        switch(m.getAuthMessageType()) {
            case UNSET:
            case AUTH_LOGIN:
            case AUTH_REGISTER:
            case UNRECOGNIZED:
                break;

            case AUTH_SUCCESS:
                goOnline();
                break;

            case AUTH_ERROR_USER:
                if(loginScreen != null) {
                    loginScreen.showUserErrorMessage();
                }
                break;

            case AUTH_ERROR_PASSWORD:
                if(loginScreen != null) {
                    loginScreen.showPasswordErrorMessage();
                }
                break;
        }
    }

    public void login(String hostname, int port, String userName, String password) {
        if(loginOrRegister(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_LOGIN, hostname, port, userName, password)) {
            // Login request sent.
        } else {
            // Login request was not sent.
        }
    }

    public void register(String hostname, int port, String userName, String password) {
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
    private boolean loginOrRegister(NetMessage.Message.AuthenticationMessage.AuthMessageType messageType,
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
            userName = userName;
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
            channel = client.run(this, message);
        }
        return false;
    }

    /**
     * Closes the login screen and opens the chat screen.
     */
    public void goOnline() {
        if(loginScreen != null) {
            loginScreen.hide();
            chatScreen.show();
//            loginScreen.close();
//            loginScreen = null;
//            if(chatScreen != null) {
//                chatScreen.close();;
//                System.err.println("Error: goOnline() found a ChatScreen already in place!");
//            }
//            chatScreen = new ChatScreen();

        } // Else we're already online.
    }

    /**
     * Closes the connection to the server and returns to the login screen.
     */
    public void goOffline() {
        chatScreen.hide();
        loginScreen.show();
//        if(chatScreen != null) {
//            chatScreen.close();
//            chatScreen = null;
//            shutdown();
//            if(loginScreen != null) {
//                loginScreen.close();
//                System.err.println("ClientController.goOffline() found a LoginScreen already in place!");
//            }
//            loginScreen = LoginScreen.createAndShow();
//        }
    }

    /**
     * Closes the server connection.
     */
    public void shutdown() {
        if(client != null) {
            client.stop();
            client = null;
        }
    }

    /**
     * Wrapper for LoginScreen's error display mechanism.
     * @param errorText A SHORT (one-line) description of the error.
     */
    public void showLoginError(String errorText) {
        loginScreen.showErrorMessage(errorText);
    }

    private void processChatMessage(NetMessage.Message.ChatMessage message) {
        if (message == null
                || message.getChatMessageType() == NetMessage.Message.ChatMessage.ChatMessageType.UNSET
                || message.getUserCase() != NetMessage.Message.ChatMessage.UserCase.SENDER
                || message.getText().trim().equals("")) {

            // A field is flatly missing.
            return;

        } else if (chatScreen == null) {

            // We're in the wrong state! We can't display this, and we really shouldn't have received it.
            System.err.println("Received chat message while not in chat mode:");
            System.err.println(message.toString());
            System.err.println();
            return;
        }

        // Else, the message has everything it needs: a type, a sender, and a text body.
        switch(message.getChatMessageType()) {
            case PUBLIC:
                chatScreen.addPublicMessage(message.getSender(), message.getText());
                // TODO Record public message on client if we're doing that.
                break;

            case PRIVATE:
                // TODO Implement private messages.
                break;

            case UNRECOGNIZED:
                System.err.println("Received chat message with unrecognized ChatMessageType. Ignoring.");
        }
    }

    public void sendPublicMessage(String message) {
        if(message == null || message.equals("")) {
            return;
        }

        if(channel == null) {
            System.err.println("Tried to send a public message without a valid channel. Ignoring.");
            return;
        }

        channel.writeAndFlush(
                NetMessage.Message.newBuilder()
                        .setChatMessage(
                                NetMessage.Message.ChatMessage.newBuilder()
                                        .setChatMessageType(NetMessage.Message.ChatMessage.ChatMessageType.PUBLIC)
                                        .setText(message)
                                        .build()
                        )
                        .build()
        );
        System.out.println("Public message sent!");
    }
}
