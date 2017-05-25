package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.gui.ChatHistoryScreen;
import com.dylanlaufenberg.portlandstate.cs300.gui.ChatScreen;
import com.dylanlaufenberg.portlandstate.cs300.gui.LoginScreen;
import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.Channel;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Controller class for the client application. Handles the business logic and manages the GUI states.
 */
public class ClientController {

    private static final String logFileDirectory = "log";

    private static ChatClient client;
    private static Channel channel;
    public static String userName;
    public static ChatScreen chatScreen;
    public static LoginScreen loginScreen;
    public static File logFile;
    private static FileOutputStream logger;

    public static void main(String[] args) throws Exception {
        loginScreen = LoginScreen.createAndShow(); // FIXME Refactor and reorganize appropriately
        chatScreen = new ChatScreen();
        loginScreen.show();
    }

    /**
     * Attempts to close the current log file, if any, and open or create the log file for the current user
     * (as specified by ClientController.userName). Displays errors to System.err, but other error reporting
     * may be desired.
     *
     * A log file for a user is named after the userName, with a .log extension.
     * @return True if the new log file is open, false otherwise. If false, ClientController.logFile will be null.
     */
    private static boolean configureLog() {

        // Construct the File representation in Java.
        String logFileName = logFileDirectory + "/" + userName + ".log";
        logFile = new File(logFileName);

        try {
            // Try to open the file for writing.
            logger = new FileOutputStream(logFile, true);
        } catch(FileNotFoundException e) {
            logFile = null;
            logger = null;
            System.err.println("Could not open log file " + logFileName + " for writing.");
        }

        return logger != null;
    }

    private static void closeLog() {
        if(logger == null) {
            return;
        }

        try {
            logger.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        logger = null;
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
        if(!configureLog()) {
            // Log couldn't be configured. We need to display a message to the user and then continue.
            JOptionPane.showMessageDialog(null, "Could not open log file for writing. This session will not be recorded locally.");
        }
        loginScreen.hide();
        chatScreen.show();
    }

    /**
     * Closes the connection to the server and returns to the login screen.
     */
    public static void goOffline() {
        closeLog();
        chatScreen.hide();
        loginScreen.show();
        shutdown();
    }

    /**
     * Closes the server connection and the log file (if one is open).
     */
    public static void shutdown() {
        if(client != null) {
            client.stop();
            client = null;
        }
        if(logger != null) {
            try {
                logger.flush();
                logger.close();
            } catch(IOException e) {
                System.err.println(e.toString());
            }
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
                break;

            case PRIVATE:
                chatScreen.addPrivateMessage(message.getSender(), message.getText(), false);
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

    public static void log(String message) {
        if(logger != null && message != null && !message.equals("")) {
            try {
                logger.write(message.getBytes());
                logger.flush();
            } catch(IOException e) {
                System.err.println(e.toString());
            }
        }
    }

    public static void showLog() {
        if(logFile == null) {
            JOptionPane.showMessageDialog(null, "No log file is available.");
            return;
        }

        if(!logFile.canRead()) {
            JOptionPane.showMessageDialog(null, "Cannot read from log file.");
            return;
        }

        if(userName == null || userName.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Cannot load chat history for blank user.");
            return;
        }

        ChatHistoryScreen.screenFor(logFile, userName);
    }
}
