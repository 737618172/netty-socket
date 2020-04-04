package com.netty_websocket.im.service;

import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import io.netty.channel.ChannelHandlerContext;

public interface MessageProxy {

    MessageWrapper convertToMessageWrapper(String ctx, MessageProto.Model message);
}

