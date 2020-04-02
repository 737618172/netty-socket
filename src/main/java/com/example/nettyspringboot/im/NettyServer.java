package com.example.nettyspringboot.im;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NettyServer implements CommandLineRunner {

    @Autowired
    private NettyWebSocketServer nettyServer;

    @Override
    public void run(String... args) throws Exception {
//        new Thread(()->new NettyWebSocketServer().run()).start();
        nettyServer.run();
    }
}
