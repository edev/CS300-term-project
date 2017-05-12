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

    public static User loadUser(String userName, String password) {
        // TODO Load the user from persistence, once we have some....
        return null;
    }

    public static User newUser(String userName, String password) {
        // TODO Check for existing User in persistence, once we have some....
        // if(user exists) { return null; }

        return new User(userName, password);
    }

    private User(String userName, String password) {
        name = userName;
        this.password = password;
    }
}
