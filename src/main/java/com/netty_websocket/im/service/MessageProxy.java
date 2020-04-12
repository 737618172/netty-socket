package com.netty_websocket.im.service;

import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import io.netty.channel.ChannelHandlerContext;

public interface MessageProxy {

    MessageWrapper convertToMessageWrapper(String ctx, MessageProto.Model message);

    MessageProto.Model  getOnLineStateMsg(String sessionId);

    MessageProto.Model getCustomerConnMsg(String cusSessionId);

    MessageProto.Model getServerConnMsg(String serSessionId);

    /**
     * 保存在线消息
     * @param message
     */
    void  saveOnlineMessageToDB(MessageWrapper message);
    /**
     * 保存离线消息
     * @param message
     */
    void  saveOfflineMessageToDB(MessageWrapper message);

    /**
     * 重连信息
     * @param sessionId
     * @return
     */
    MessageWrapper  getReConnectionStateMsg(String sessionId);


    MessageWrapper  getOfflineMsg( );
}

