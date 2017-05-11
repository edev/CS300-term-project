package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.Channel;

/**
 * Controller for the server program. Receives and routes incoming requests from ChatConnections.
 */
class ServerController {

    public static User process(User user, NetMessage.Message message, Channel channel) {
        // TODO Do something useful
        System.out.println("Received message: ");
        System.out.println(message.toString());
        return user;
    }
}
