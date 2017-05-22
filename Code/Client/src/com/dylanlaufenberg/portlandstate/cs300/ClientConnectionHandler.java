package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.swing.*;

/**
 * Defines the handler used to respond to state changes and data reads on the Netty Channel talking to the server.
 */
public class ClientConnectionHandler extends SimpleChannelInboundHandler<NetMessage.Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage.Message m) throws Exception {
        if(m != null && m.getMessageContentsCase() != NetMessage.Message.MessageContentsCase.MESSAGECONTENTS_NOT_SET) {
            System.out.println("Received message: ");
            System.out.println(m.toString());
            System.out.println();
            SwingUtilities.invokeLater(() -> {
                ClientController.processMessage(m);
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ClientController.showLoginError(cause.toString());
    }
}
