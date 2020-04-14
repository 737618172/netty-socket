package com.netty_websocket;

import com.netty_websocket.im.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(NettyServer.class)
public class NettyspringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyspringbootApplication.class, args);
    }

}
