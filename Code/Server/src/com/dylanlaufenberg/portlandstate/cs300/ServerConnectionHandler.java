package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Defines the handler for a network connection to a single client.
 */
class ServerConnectionHandler extends SimpleChannelInboundHandler<NetMessage.Message> {
    private User user;

    /**
     * Passes messages to ServerController, and then checks to make sure the Channel still corresponds to a User.
     * If not, closes the connection.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetMessage.Message m) throws Exception {
        user = ServerController.process(user, m, ctx.channel());
        if(user == null) {
            // Channel does not correspond to a user. Close it.
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Exception caught: " + cause.toString());
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if(user != null) {
            ServerController.users.remove(user.name);
            user.broadcast.writeAndFlush(
                    ServerHelper.buildNoticeMessage(
                            NetMessage.Message.NoticeMessage.NoticeMessageType.OFFLINE,
                            user.name
                    )
            );
            // ChannelGroups, i.e. ServerController.channels and user.broadcast, will be cleaned up automatically.
        }
    }
}
