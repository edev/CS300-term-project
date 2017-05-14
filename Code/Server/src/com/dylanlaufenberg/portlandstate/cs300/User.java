package com.dylanlaufenberg.portlandstate.cs300;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

/**
 * Defines the model for a user of the chat server and provides an interface between persistent data and Java objects.
 */
public class User {
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
            BAD_PASSWORD    // user will be null.
        }
    }

    public static AuthResult loadUser(String userName, String password) {
        // TODO Load the user from persistence, once we have some....
        return null;
    }

    public static AuthResult newUser(String userName, String password) {
        // TODO Check for existing User in persistence, once we have some....
        // if(user exists) { return a BAD_USER result; }

        AuthResult ar = new AuthResult();
        ar.user = new User(userName, password);
        ar.result = AuthResult.Result.SUCCESS;
        return ar;
    }

    private User(String userName, String password) {
        name = userName;
        this.password = password;
    }
}
