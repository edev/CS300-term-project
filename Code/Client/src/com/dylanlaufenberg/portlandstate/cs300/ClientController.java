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
            SharedHelper.error("Could not open log file " + logFileName + " for writing.");
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

    public static boolean processMessage(NetMessage.Message m) {
        if(m == null) {
            return false;
        }

        switch(m.getMessageContentsCase()) {
            case AUTHMESSAGE:
                return processAuthMessage(m.getAuthMessage());

            case NOTICEMESSAGE:
                return processNotice(m.getNoticeMessage());

            case CHATMESSAGE:
                return processChatMessage(m.getChatMessage());

            case USERLIST:
                return processUserList(m.getUserList());

            default:
                SharedHelper.error("Received a message of an unknown type:", m.toString());
                return false;
        }
    }

    private static boolean processAuthMessage(NetMessage.Message.AuthenticationMessage m) {
        if(m == null) {
            return false;
        }

        switch(m.getAuthMessageType()) {
            case AUTH_SUCCESS:
                goOnline();
                return true;

            case AUTH_ERROR_USER:
                if(loginScreen != null) {
                    loginScreen.showUserErrorMessage();
                }
                return false;

            case AUTH_ERROR_PASSWORD:
                if(loginScreen != null) {
                    loginScreen.showPasswordErrorMessage();
                }
                return false;

            default:
                return false;
        }
    }

    public static boolean login(String hostname, int port, String userName, String password) {
        return loginOrRegister(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_LOGIN, hostname, port, userName, password);
    }

    public static boolean register(String hostname, int port, String userName, String password) {
        return loginOrRegister(NetMessage.Message.AuthenticationMessage.AuthMessageType.AUTH_REGISTER, hostname, port, userName, password);
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
                || port > 65535
                || userName == null
                || userName.length() == 0
                || password == null
                || password.length() == 0) {
            // Error detected.
            SharedHelper.error("Called ClientController.loginOrRegister(...) with invalid arguments. Ignoring:\n" +
                    "messageType:\n" +
                    (messageType == null ? "null" : messageType.toString()) +
                    "hostname: " + hostname + "\n" +
                    "port: " + port + "\n" +
                    "userName: " + userName + "\n" +
                    "password: (" + (password == null ? "null" : password.length()) + " characters)");
        } else {
            ClientController.userName = userName;
            NetMessage.Message message = SharedHelper.buildAuthMessage(messageType, userName, password);

            if(client != null) {
                client.stop();
            }

            client = new ChatClient();
            client.host = hostname;
            client.port = port;
            channel = client.run(message);
            return true;
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

        if(loginScreen != null) {
            loginScreen.clearErrorMessage();
            loginScreen.hide();
        } else {
            SharedHelper.error("Tried to goOnline() without a valid loginScreen.");
        }

        if(chatScreen != null) {
            chatScreen.show();
        } else {
            SharedHelper.error("Tried to goOnline() without a valid chatScreen.");
        }
    }

    /**
     * Closes the connection to the server and returns to the login screen.
     */
    public static void goOffline() {
        closeLog();

        if(chatScreen != null) {
            chatScreen.hide();
        } else {
            SharedHelper.error("Tried to goOnline() without a valid chatScreen.");
        }

        if(loginScreen != null) {
            loginScreen.show();
        } else {
            SharedHelper.error("Tried to goOnline() without a valid loginScreen.");
        }
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
                SharedHelper.error(e.toString());
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

    private static boolean processNotice(NetMessage.Message.NoticeMessage message) {
        if(message == null
                || message.getUserName().equals("")) {
            // Required information is missing.
            return false;
        }

        if(chatScreen == null) {
            SharedHelper.error("Received notice message without a valid chatScreen:", message.toString());
            return false;
        }

        switch(message.getNoticeMessageType()) {
            case ONLINE:
                chatScreen.userAdded(message.getUserName());
                return true;

            case OFFLINE:
                chatScreen.userRemoved(message.getUserName());
                return true;

            default:
                SharedHelper.error("Received invalid notice message: ", message.toString());
                return false;

        }
    }

    private static boolean processChatMessage(NetMessage.Message.ChatMessage message) {
        if (message == null
                || message.getChatMessageType() == NetMessage.Message.ChatMessage.ChatMessageType.UNSET
                || message.getUserCase() != NetMessage.Message.ChatMessage.UserCase.SENDER
                || message.getText().trim().equals("")) {

            // A field is flatly missing.
            return false;

        } else if (chatScreen == null) {

            // We're in the wrong state! We can't display this, and we really shouldn't have received it.
            SharedHelper.error("Received chat message while not in chat mode:", message.toString());
            return false;
        }

        // Else, the message has everything it needs: a type, a sender, and a text body.
        switch(message.getChatMessageType()) {
            case PUBLIC:
                chatScreen.addPublicMessage(message.getSender(), message.getText());
                return true;

            case PRIVATE:
                chatScreen.addPrivateMessage(message.getSender(), message.getText(), false);
                return true;

            default:
                SharedHelper.error("Received chat message with unrecognized ChatMessageType. Ignoring.");
                return false;
        }
    }

    private static boolean processUserList(NetMessage.Message.UserList users) {
        if(users == null) {
            SharedHelper.error("processUserList received a null UserList.");
            return false;
        }

        if(chatScreen == null) {
            SharedHelper.error("Received UserList without a valid chatScreen:", users);
            return false;
        }

        // Note: users.getUserCount() == 0 is a valid state, and the loop handles it correctly.
        for(String user : users.getUserList()) {
            chatScreen.userAdded(user);
        }
        return true;
    }

    public static boolean sendPublicMessage(String message) {
        if(message == null || message.equals("")) {
            SharedHelper.error("Tried to send public message without a message body.");
            return false;
        }

        if(channel == null) {
            SharedHelper.error("Tried to send a public message without a valid channel. Ignoring.");
            return false;
        }

        channel.writeAndFlush(
                SharedHelper.buildPublicMessage(message)
        );
        return true;
    }

    public static boolean sendPrivateMessage(String receiver, String message) {
        if(message == null
                || message.trim().equals("")) {
            SharedHelper.error("Tried to send private message without a message body.");
            return false;
        }

        if(receiver == null
            || receiver.trim().equals("")) {
            SharedHelper.error("Tried to send private message without specifying a receiver.");
            return false;
        }

        if(channel == null) {
            SharedHelper.error("Tried to send a public message without a valid channel. Ignoring.");
            return false;
        }

        channel.writeAndFlush(
                SharedHelper.buildPrivateMessage(receiver, message)
        );
        return true;
    }

    public static void log(String message) {
        if(logger != null && message != null && !message.equals("")) {
            try {
                logger.write(message.getBytes());
                logger.flush();
            } catch(IOException e) {
                SharedHelper.error(e.toString());
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
