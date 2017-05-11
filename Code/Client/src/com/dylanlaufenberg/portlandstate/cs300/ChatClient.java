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
    private ChannelFuture future;

    public Channel run() {
        if(future != null) {
            stop();
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
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
            future = b.connect(host, port).sync(); // (5)
            return future.channel();
        } catch (Exception e) {
            System.err.println(e.toString());
            workerGroup.shutdownGracefully();
            return null;
        }
    }

    public void stop() {
        if(future != null) {
            try {
                // Wait until the connection is closed.
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                future = null;
            }
        }
    }
}
