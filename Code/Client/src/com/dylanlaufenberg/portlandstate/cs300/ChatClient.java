package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * Bootstraps the Netty server.
 */
public class ChatClient {

    public String host;
    public int port;
    private Channel channel;
    private EventLoopGroup workerGroup;

    public Channel run(NetMessage.Message firstMessage) {
        if(channel != null) {
            stop();
        }

        workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    // Decoders
                    pipeline.addLast("frameDecoder",
                            new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
                    pipeline.addLast("ProtobufDecoder", new ProtobufDecoder(NetMessage.Message.getDefaultInstance()));
                    pipeline.addLast("ChannelInboundHandler", new ChatHandler());

                    // Encoders
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                    pipeline.addLast("ProtobufEncoder", new ProtobufEncoder());
                }
            });

            // Start the client.
            ChannelFuture future = b.connect(host, port);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if(f.isSuccess()) {
                        if(firstMessage != null) {
                            System.out.println("Successfully connected! Sending message:");
                            System.out.println(firstMessage.toString());
                            System.out.println();

                            f.channel().writeAndFlush(firstMessage);
                        }
                    } else {
                        ClientController.showLoginError(f.cause().getMessage());
                    }
                }
            });

            channel = future.channel();

            return channel;
        } catch (Exception e) {
            System.err.println(e.toString());
            workerGroup.shutdownGracefully();
            return null;
        }
    }

    public void stop() {
        if(channel != null) {
            // Wait until the connection is closed.
            channel.close();
            channel = null;
        }
        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }
}
