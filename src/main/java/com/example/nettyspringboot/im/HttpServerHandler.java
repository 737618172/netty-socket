package com.example.nettyspringboot.im;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.nio.charset.Charset;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

//    @Autowired
//    private MessageService messageService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        System.out.println("123");
        if (msg instanceof HttpRequest) {
            HttpRequest obj = (HttpRequest) msg;
            System.out.println(obj.uri() + "-----" +msg.getClass());
            if (!"/chat".equals(obj.uri())) {
                return;
            }
        }
        ByteBuf helloworld = Unpooled.copiedBuffer("helloworld", Charset.defaultCharset());

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, helloworld);
//        HttpServletResponse res = (HttpServletResponse) response;
//        res.addCookie("da","d");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, helloworld.readableBytes());
        ctx.channel().writeAndFlush(response);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"加入");
//        String uId = UUID.randomUUID().toString();
//        Attribute<String> channelAttr = ctx.channel().attr(key);
//        Attribute<String> contextAttr = ctx.attr(key);

        ctx.writeAndFlush(new TextWebSocketFrame(ctx.channel().remoteAddress() +"已连接"));
    }
}
