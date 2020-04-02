package com.netty_websocket.im;

import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.service.MessageProxy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;


public class SocketChannelInitializer extends ChannelInitializer {

    private ProtobufDecoder decoder = new ProtobufDecoder(MessageProto.Model.getDefaultInstance());

    private MessageProxy messageProxy;

    public SocketChannelInitializer(MessageProxy messageProxy){
        this.messageProxy = messageProxy;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast( new HttpServerCodec());

        pipeline.addLast(new HttpObjectAggregator(Constants.ImserverConfig.MAX_AGGREGATED_CONTENT_LENGTH));
        // 主要用于处理大数据流，比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的; 增加之后就不用考虑这个问题了
        pipeline.addLast(new ChunkedWriteHandler());
        // WebSocket数据压缩
        pipeline.addLast(new WebSocketServerCompressionHandler());
        // 协议包长度限制
        pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true, Constants.ImserverConfig.MAX_FRAME_LENGTH));

        // 协议包解码
        pipeline.addLast(new MessageToMessageDecoder<WebSocketFrame>() {
            @Override
            protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> objs) throws Exception {
                ByteBuf buf = ((BinaryWebSocketFrame) frame).content();
                objs.add(buf);
                buf.retain();
            }
        });

        pipeline.addLast(decoder);
        pipeline.addLast(new IdleStateHandler(30,30,60 * 30));
        pipeline.addLast(new WebSocketHandler(messageProxy));
    }
}
