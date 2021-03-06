package com.netty_websocket.im;

import com.netty_websocket.im.service.ImConnector;
import com.netty_websocket.im.service.MessageProxy;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyWebSocketServer {

    @Autowired
    private MessageProxy messageProxy;

    @Autowired
    private ImConnector connector;

    public  void run() {
        EventLoopGroup bossGroup = null;
        EventLoopGroup workerGroup = null;
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new SocketChannelInitializer(messageProxy,connector));

            System.out.println("服务器启动");
            // start
            ChannelFuture future = boot.bind(8040);
            future.channel().closeFuture().sync();
//            System.out.println("服务器关闭");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // shutdown
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
