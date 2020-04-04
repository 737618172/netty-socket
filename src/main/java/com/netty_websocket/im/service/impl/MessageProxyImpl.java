package com.netty_websocket.im.service.impl;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.MessageBodyProto;
import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.service.MessageProxy;
import com.netty_websocket.im.model.MessageWrapper;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

@Service
public class MessageProxyImpl implements MessageProxy {
    private final static Logger log = LoggerFactory.getLogger(MessageProxyImpl.class);

    @Autowired
    private RedisTemplate redisTemplate;

    public MessageWrapper convertToMessageWrapper(String sessionId, MessageProto.Model message) {

        switch (message.getCmd()) {
            case Constants.CmdType.BIND:
                //  设置通道session
//                ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).set(message.getSender());
//
//                if(Constants.UserType.SERVICER == message.getUtype()){
//
//                }else if (Constants.UserType.SERVICER == message.getUtype()){
//                    redisTemplate.opsForSet().add("servicer",message.get);
//                }
                try {
//                    Set members = redisTemplate.opsForSet().members("10891");
//                    if(members!=null && members.size()>0){
//                        Iterator iterator = members.iterator();
//                        while(iterator.hasNext()){
//
//                        }
//
//                    }

                    return new MessageWrapper(MessageWrapper.MessageProtocol.CONNECT, message.getSender(), null, message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Constants.CmdType.HEARTBEAT:
                try {
                    return new MessageWrapper(MessageWrapper.MessageProtocol.HEART_BEAT, sessionId, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Constants.CmdType.ONLINE:

                break;
            case Constants.CmdType.OFFLINE:

                break;
            case Constants.CmdType.MESSAGE:
                try {
                    MessageProto.Model.Builder result = MessageProto.Model.newBuilder(message);
                    result.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    result.setSender(sessionId);//存入发送人sessionId
                    message = MessageProto.Model.parseFrom(result.build().toByteArray());
                    //判断消息是否有接收人
                    if (StringUtils.isNotEmpty(message.getReceiver())) {
                        //判断是否发消息给机器人
                        if (message.getReceiver().equals(Constants.ImserverConfig.REBOT_SESSIONID)) {
                            MessageBodyProto.MessageBody msg = MessageBodyProto.MessageBody.parseFrom(message.getContent());
//								  return  rebotProxy.botMessageReply(sessionId, msg.getContent());
                            return null;
                        } else {
                            return new MessageWrapper(MessageWrapper.MessageProtocol.REPLY, sessionId, message.getReceiver(), message);
                        }
                    } else if (StringUtils.isNotEmpty(message.getGroupId())) {
                        return new MessageWrapper(MessageWrapper.MessageProtocol.GROUP, sessionId, null, message);
                    } else {
                        return new MessageWrapper(MessageWrapper.MessageProtocol.SEND, sessionId, null, message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }
}
