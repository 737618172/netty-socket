package com.netty_websocket.im;

import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import com.netty_websocket.im.model.Session;
import com.netty_websocket.im.service.ImConnertor;
import com.netty_websocket.im.service.MessageProxy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

@Component
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<MessageProto.Model> {
    public static ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    public static ConcurrentHashMap<String,Object> USER_CLIENT = new ConcurrentHashMap<>();

    private MessageProxy messageProxy;
    private ImConnertor connertor;

    public WebSocketHandler(MessageProxy messageProxy, ImConnertor connertor){
        this.messageProxy = messageProxy;
        this.connertor = connertor;
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object o) throws Exception {
        String sessionId = ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
        if (o instanceof IdleStateEvent && ((IdleStateEvent) o).state().equals(IdleState.WRITER_IDLE)) {
            System.out.println("发送心跳检测");
            if(StringUtils.isNotEmpty(sessionId)){
                MessageProto.Model.Builder builder = MessageProto.Model.newBuilder();
                builder.setCmd(Constants.CmdType.HEARTBEAT);
                builder.setMsgtype(Constants.ProtobufType.SEND);
                ctx.channel().writeAndFlush(builder);
            }
            log.debug(IdleState.WRITER_IDLE +"... from "+sessionId+"-->"+ctx.channel().remoteAddress()+" nid:" +ctx.channel().id().asShortText());
        }

        //如果心跳请求发出70秒内没收到响应，则关闭连接
        if ( o instanceof IdleStateEvent && ((IdleStateEvent) o).state().equals(IdleState.READER_IDLE)){
            System.out.println("发送心跳read检测");
            log.debug(IdleState.READER_IDLE +"... from "+sessionId+" nid:" +ctx.channel().id().asShortText());
            Long lastTime = (Long) ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).get();
            if(lastTime == null || ((System.currentTimeMillis() - lastTime)/1000>= Constants.ImserverConfig.PING_TIME_OUT)){
                connertor.close(ctx);
            }
            //ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(null);
        }
    }

    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
    protected void channelRead0(ChannelHandlerContext ctx, MessageProto.Model msg) throws Exception {

        String sessionId = msg.getToken();
//        String sessionId = ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
        MessageWrapper messageWrapper = messageProxy.convertToMessageWrapper(sessionId, msg);

        if(messageWrapper.isOnline()){
            connertor.connect(ctx,messageWrapper);
        }else if(messageWrapper.isClose()){
            connertor.close(ctx);
        }else if(messageWrapper.isSend()){
            connertor.pushMessage(messageWrapper);
        }else if(messageWrapper.isHeartbeat()){
            connertor.heartbeatToClient(ctx,messageWrapper);
        }else if(messageWrapper.isReply()){
            connertor.pushMessage(messageWrapper);
        }
    }
}
