package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Defines the handler used to respond to state changes and data reads on the Netty Channel talking to the server.
 */
public class ChatHandler extends SimpleChannelInboundHandler<NetMessage.Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage.Message m) throws Exception {

    }
}
