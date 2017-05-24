package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.gui.ChatScreen;
import com.dylanlaufenberg.portlandstate.cs300.gui.LoginScreen;
import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.Channel;

import java.io.File;

/**
 * Controller class for the client application. Handles the business logic and manages the GUI states.
 */
public class ClientController {

    private static ChatClient client;
    private static Channel channel;
    public static String userName;
    public static ChatScreen chatScreen;
    public static LoginScreen loginScreen;
    public static File logFile;

    public static void main(String[] args) throws Exception {
        parseCommandLineOptions(args);

        loginScreen = LoginScreen.createAndShow(); // FIXME Refactor and reorganize appropriately
        chatScreen = new ChatScreen();
        loginScreen.show();
    }

    /**
     * Parses command-line options. Valid options:
     * --log LOG_PATH   Specifies that chat history should be logged to and retrieved from LOG_PATH,
     *                  which should be a destination .log file. (One will be created if it does not already exist.)
     * --nolog          Disables chat history logging for the session.
     * @param args Command-line arguments, passed through from main.
     */
    private static void parseCommandLineOptions(String[] args) {
        // I don't really have a plan for parsing these, nor many options to consider, so this may be a bit haphazard.
        // If I find that I intend to implement a lot of stuff, I'll import a library to handle it.
        for(int i = 0; i < args.length; ++i) {
            switch(args[i]) {
                case "--log":
                    // TODO Implement.
                    break;
                case "--nolog":
                    // TODO Implement.
                    break;
            }
        }
    }

    /**
     * Parses "--log PATH" entries.
     * @param args args from main.
     * @param index index of PATH in args. Out-of-bounds index IS ALLOWED and will be checked.
     */
    private static void parseOptionLog(String args[], int index) {
        if(index >= args.length) {
            System.err.println("Found --log option without path. Ignoring.");
            return;
        }

        logFile = new File(args[index]);
        // TODO Figure out what kind of IO to use and implement it, including error reporting if there's a problem.
        // TODO Create separate --log options for two different run configurations.
        // TODO Verify behavior of two run attempts on the same file.

    }

    public static void processMessage(NetMessage.Message m) {
        switch(m.getMessageContentsCase()) {
            case AUTHMESSAGE:
                processAuthMessage(m.getAuthMessage());
                break;

            case NOTICEMESSAGE:
                processNotice(m.getNoticeMessage());
                break;

            case CHATMESSAGE:
                processChatMessage(m.getChatMessage());
                break;

            case USERLIST:
                processUserList(m.getUserList());
                break;

            default:
                System.err.println("Received a message of an unknown type:");
                System.err.println(m.toString());
        }
    }

    private static void processAuthMessage(NetMessage.Message.AuthenticationMessage m) {
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
            ClientController.userName = userName;
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
            channel = client.run(message);
        }
        return false;
    }

    /**
     * Closes the login screen and opens the chat screen.
     */
    public static void goOnline() { // TODO Refactor goOnline and goOffline with shutdown; integrate more closely with netty status.
        loginScreen.hide();
        chatScreen.show();
    }

    /**
     * Closes the connection to the server and returns to the login screen.
     */
    public static void goOffline() {
        chatScreen.hide();
        loginScreen.show();
        shutdown();
    }

    /**
     * Closes the server connection.
     */
    public static void shutdown() {
        if(client != null) {
            client.stop();
            client = null;
        }
    }

    public static void exit() {
        shutdown();
        System.exit(0);
    }

    /**
     * Wrapper for LoginScreen's error display mechanism.
     * @param errorText A SHORT (one-line) description of the error.
     */
    public static void showLoginError(String errorText) {
        loginScreen.showErrorMessage(errorText);
    }

    private static void processNotice(NetMessage.Message.NoticeMessage message) {
        if(message == null
                || message.getUserName().equals("")) {
            // Required information is missing.
            return;
        }

        switch(message.getNoticeMessageType()) {
            case ONLINE:
                chatScreen.userAdded(message.getUserName());
                break;

            case OFFLINE:
                chatScreen.userRemoved(message.getUserName());
                break;

            case UNSET: // TODO Make sure other processXMessage switches have this triple-default-case structure, too. Or simply default.
            case UNRECOGNIZED:
            default:
                System.err.println("Received invalid notice message: ");
                System.err.println(message.toString());
                break;

        }
    }

    private static void processChatMessage(NetMessage.Message.ChatMessage message) {
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
                chatScreen.addPrivateMessage(message.getSender(), message.getText(), false);
                // TODO Record private message on client if we're doing that.
                break;

            case UNRECOGNIZED:
                System.err.println("Received chat message with unrecognized ChatMessageType. Ignoring.");
        }
    }

    private static void processUserList(NetMessage.Message.UserList users) {
        if(chatScreen == null) {
            return;
        }

        for(String user : users.getUserList()) {
            chatScreen.userAdded(user);
        }
    }

    public static void sendPublicMessage(String message) {
        if(message == null || message.equals("")) {
            System.err.println("Tried to send public message without a message body..");
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
    }

    public static void sendPrivateMessage(String receiver, String message) {
        if(message == null
                || message.trim().equals("")) {
            System.err.println("Tried to send private message without a message body.");
            return;
        }

        if(receiver == null
            || receiver.trim().equals("")) {
            System.err.println("Tried to send private message without specifying a receiver.");
        }

        if(channel == null) {
            System.err.println("Tried to send a public message without a valid channel. Ignoring.");
            return;
        }

        channel.writeAndFlush(
                NetMessage.Message.newBuilder()
                        .setChatMessage(
                                NetMessage.Message.ChatMessage.newBuilder()
                                        .setChatMessageType(NetMessage.Message.ChatMessage.ChatMessageType.PRIVATE)
                                        .setReceiver(receiver)
                                        .setText(message)
                                        .build()
                        )
                        .build()
        );
    }
}
