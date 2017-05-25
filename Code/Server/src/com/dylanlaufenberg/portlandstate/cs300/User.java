package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Defines the model for a user of the chat server and provides an interface between persistent data and Java objects.
 */
public class User {
    private static String userFileName = "users.dat";
    public String name;
    private String password;
    public Channel channel;
    public ChannelGroup broadcast;

    public static class AuthResult {
        User user;
        Result result;

        enum Result {
            SUCCESS,        // user will be set.
            BAD_USER,       // user will be null.
            BAD_PASSWORD,    // user will be null.
        }
    }

    // A balanced tree storing ALL known users. Loaded at server load, maintained through server life.
    // Always the same as the persisted user data.
    private static SortedMap<String, User> users = Collections.synchronizedSortedMap((new TreeMap<String, User>()));

    private static FileOutputStream fout;

    public static AuthResult loadUser(String userName, String password) {
        AuthResult result = new AuthResult();
        User user;
        if(userName == null
                || userName.trim().equals("")) {
            // Invalid userName.
            result.result = AuthResult.Result.BAD_USER; // Client will think user already exists, but this is just a safety check anyway.

        } else if(password == null
                ||password.trim().equals("")) {
            // Invalid password.
            result.result = AuthResult.Result.BAD_PASSWORD;

        } else {
            // Okay, we're clear to look for the user and validate.

            user = users.get(keyFor(userName));

            if (user == null) {
                // User doesn't exist.
                result.result = AuthResult.Result.BAD_USER; // Client knows this means user doesn't exist.
            } else {
                // Okay, valid request for a user, and user is already loaded into our local variable!
                // Now, check password.
                if (!user.password.equals(password)) {
                    result.result = AuthResult.Result.BAD_PASSWORD;
                } else {
                    // Successful login!
                    result.user = user;
                    result.result = AuthResult.Result.SUCCESS;
                }
            }
        }

        return result;
    }

    public static AuthResult newUser(String userName, String password) {
        AuthResult result = new AuthResult();

        if(userName == null
                || userName.trim().equals("")) {
            // Invalid userName.
            result.result = AuthResult.Result.BAD_USER; // Client will think user already exists, but this is just a safety check anyway.

        } else if(password == null
                ||password.trim().equals("")) {
            // Invalid password.
            result.result = AuthResult.Result.BAD_PASSWORD;

        } else if(users.containsKey(keyFor(userName))) {
            // User already exists.
            result.result = AuthResult.Result.BAD_USER; // Client knows this means user already exists.

        } else {
            // Okay, valid request for a new user!

            // Save user to file.
            try {
                buildRecord(userName, password).writeDelimitedTo(fout);
            } catch(IOException e) {
                System.err.println("Cannot write user record to disk. Printing stack trace.");
                e.printStackTrace();
            }

            result.user = new User(keyFor(userName), password);
            result.result = AuthResult.Result.SUCCESS;
        }

        return result;
    }

    private static NetMessage.Message buildRecord(String userName, String password) {
        if(userName == null || password == null || userName.trim().equals("") || password.trim().equals("")) {
            throw new NullPointerException();
        }

        return NetMessage.Message.newBuilder()
                .setAuthMessage(
                        NetMessage.Message.AuthenticationMessage.newBuilder()
                                .setUserName(userName)
                                .setPassword(password)
                                .build()
                )
                .build();
    }

    private User(String userName, String password) {
        name = userName;
        this.password = password;
    }

    /**
     * loadUserFile is a MISSION-CRITICAL method to read the user database into memory. If it returns false, the caller
     * MUST safely shut down the server, because the server will not work properly!
     * @return true on success, false on failure (in which case the server must safely shut down).
     */
    public static boolean loadUserFile() {
        assert userFileName != null;
        File userFile = new File(userFileName);
        FileInputStream fin;

        try {
            if (userFile.exists()) {
                System.out.println("Found " + userFileName + ". Opening to read users.");

                fin = new FileInputStream(userFile);
                NetMessage.Message message;
                message = NetMessage.Message.parseDelimitedFrom(fin); // Preload first message.
                // Load all messages into memory.
                while (message != null) {
                    // Verify that message is valid
                    if (message.getMessageContentsCase() != NetMessage.Message.MessageContentsCase.AUTHMESSAGE) {
                        System.err.println("User.loadUserFile(): read message with wrong MessageContents. Discarding:");
                        System.err.println(message.toString());
                        continue;
                    }
                    String userName = message.getAuthMessage().getUserName();
                    String password = message.getAuthMessage().getPassword();

                    if (userName.trim().equals("")) {
                        System.err.println("User.loadUserFile(): read user with no name. Discarding:");
                        System.err.println(message.toString());
                        continue;
                    }

                    if (password.trim().equals("")) {
                        System.err.println("User.loadUserFile(): read user with no name. Discarding:");
                        System.err.println(message.toString());
                        continue;
                    }

                    // Okay, it has everything we need.
                    User newUser = new User(userName, password);
                    if (users.putIfAbsent(keyFor(userName), newUser) == null) {
                        System.out.println("Loaded user: " + userName);
                    } else {
                        System.err.println("User.loadUserFile(): duplicate user found in user file: " + userName);
                    }

                    message = NetMessage.Message.parseDelimitedFrom(fin);
                } // While loop

                fin.close();
            } else {
                System.out.println(userFileName + " not found. Creating it.");
            }

            // Now set up the writer to handle registration.
            fout = new FileOutputStream(userFile, true);

        } catch(Exception e) {
            // On ANY exception, print, fail, die.
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static String keyFor(String userName) {
        if(userName == null) {
            throw new NullPointerException();
        }
        return userName.toLowerCase();
    }
}
