package com.example.nettyspringboot.im;

import com.example.nettyspringboot.im.model.MessageProto;
import com.example.nettyspringboot.im.model.MessageWrapper;
import com.example.nettyspringboot.im.service.MessageProxy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

//public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<MessageProto.Model> {
    public static ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    public static ConcurrentHashMap<String,Object> USER_CLIENT = new ConcurrentHashMap<>();

    private MessageProxy messageProxy;

    public WebSocketHandler(MessageProxy messageProxy){
        this.messageProxy = messageProxy;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"加入");

        ctx.writeAndFlush(new TextWebSocketFrame(ctx.channel().remoteAddress() +"已连接"));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"离开");
        ctx.writeAndFlush(new TextWebSocketFrame(ctx.channel().remoteAddress() + "已断开"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"离开");
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "活跃");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "不活跃");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    break;
                case WRITER_IDLE:
                    break;
                case ALL_IDLE:
                    ctx.writeAndFlush(new TextWebSocketFrame(ctx.channel().remoteAddress() + "读写空闲"));
                    ctx.close();
                    break;
                default:
                    break;
            }
        }
    }

//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
    protected void channelRead0(ChannelHandlerContext ctx, MessageProto.Model msg) throws Exception {

        MessageWrapper messageWrapper = messageProxy.convertToMessageWrapper(null, msg);


        System.out.println(ctx.channel().remoteAddress()+":"+ msg.getPlatform());
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("content",msg.text());
//        jsonObject.put("time", sdf.format(new Date()));
        ctx.writeAndFlush(new TextWebSocketFrame("服务器接收到消息  " + msg));
    }
}
