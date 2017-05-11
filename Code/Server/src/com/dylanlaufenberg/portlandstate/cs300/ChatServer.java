package com.dylanlaufenberg.portlandstate.cs300;

import com.dylanlaufenberg.portlandstate.cs300.proto.NetMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * Bootstraps the Netty server.
 */
public class ChatServer {
    private static int defaultPort = 42024;
    private int port;

    /**
     *
     * @param args Nothing, or a valid port number for the server.
     */
    public static void main(String[] args) throws Exception {
        if(args.length > 1) {
            System.err.println("Too many arguments supplied. (Valid arguments: none, or port number.)");
            System.exit(1);
        }

        int port = defaultPort;

        if(args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch(NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer port number (0 through 65535, greater than 1024 recommended).");
                System.exit(1);
            }
        }

        ChatServer server = new ChatServer(port);
    }

    public ChatServer(int port) throws Exception {
        this.port = port;

        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // Decoders
                            pipeline.addLast("frameDecoder",
                                    new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
                            pipeline.addLast("ProtobufDecoder", new ProtobufDecoder(NetMessage.Message.getDefaultInstance()));
                            pipeline.addLast("ChannelInboundHandler", new ChatConnection());

                            // Encoders
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("ProtobufEncoder", new ProtobufEncoder());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
