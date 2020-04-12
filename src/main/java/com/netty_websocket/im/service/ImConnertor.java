package com.netty_websocket.im.service;

import com.netty_websocket.im.model.MessageWrapper;
import io.netty.channel.ChannelHandlerContext;

public interface ImConnertor {
	 /**
	  * 发送心跳检测 到客户端
	  * @param hander
	  * @param wrapper
	  */
	void heartbeatToClient(ChannelHandlerContext hander, MessageWrapper wrapper);
	/**
	  * 发送消息
	  * @param wrapper
	  * @throws RuntimeException
	  */
	 void pushMessage(MessageWrapper wrapper) throws RuntimeException;

	void connect(ChannelHandlerContext ctx, MessageWrapper wrapper) ;

	/**
	 * 发送消息
	 * @param sessionId  发送人
	 * @param wrapper   发送内容
	 * @throws RuntimeException
	 */
	void pushMessage(String sessionId, MessageWrapper wrapper) throws RuntimeException;
	 /**
	  * 获取用户唯一标识符
	  * @param ctx
	  * @return
	  */
	 String getChannelSessionId(ChannelHandlerContext ctx);

	void close(ChannelHandlerContext hander);
}
