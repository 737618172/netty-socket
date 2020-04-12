package com.netty_websocket.im.service.impl;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.MessageBodyProto;
import com.netty_websocket.im.model.MessageEntity;
import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import com.netty_websocket.im.repository.MessageRepository;
import com.netty_websocket.im.service.MessageProxy;
import com.netty_websocket.im.service.SessionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MessageProxyImpl implements MessageProxy {
    private final static Logger log = LoggerFactory.getLogger(MessageProxyImpl.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public MessageWrapper convertToMessageWrapper(String sessionId, MessageProto.Model message) {

        switch (message.getCmd()) {
            case Constants.CmdType.BIND:
                try {
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
                try {
                    return new MessageWrapper(MessageWrapper.MessageProtocol.ON_LINE, message.getSender(), null, message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    }  else {
                        return new MessageWrapper(MessageWrapper.MessageProtocol.SEND, sessionId, null, message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    @Override
    public MessageProto.Model getOnLineStateMsg(String sessionId) {
        MessageProto.Model.Builder  result = MessageProto.Model.newBuilder();
        result.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        result.setSender(sessionId);//存入发送人sessionId
        result.setCmd(Constants.CmdType.ONLINE);
        return result.build();
    }

    @Override
    public MessageProto.Model getCustomerConnMsg(String cusSessionId ) {
        MessageProto.Model.Builder  result = MessageProto.Model.newBuilder();
        result.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        result.setSender(cusSessionId);//存入发送人sessionId
        result.setCmd(Constants.CmdType.BIND);

        MessageBodyProto.MessageBody.Builder  msgbody =  MessageBodyProto.MessageBody.newBuilder();
        msgbody.setContent("客户"+cusSessionId+"已连接");
        result.setContent(msgbody.build().toByteString());

        return result.build();
    }

    @Override
    public MessageProto.Model getServerConnMsg(String serSessionId ) {
        MessageProto.Model.Builder  result = MessageProto.Model.newBuilder();
        result.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        result.setSender(serSessionId);//存入发送人sessionId
        result.setCmd(Constants.CmdType.BIND);

        MessageBodyProto.MessageBody.Builder  msgbody =  MessageBodyProto.MessageBody.newBuilder();
        msgbody.setContent("客服"+ serSessionId + "为您服务");
        result.setContent(msgbody.build().toByteString());
        return result.build();
    }

    @Override
    public void saveOnlineMessageToDB(MessageWrapper message) {
        MessageEntity messageEntity = convertMessageWrapperToBean(message,1);
        messageRepository.save(messageEntity);
    }

    @Override
    public void saveOfflineMessageToDB(MessageWrapper message) {
        MessageEntity messageEntity = convertMessageWrapperToBean(message,0);
        messageRepository.save(messageEntity);
    }

    private MessageEntity convertMessageWrapperToBean(MessageWrapper message,Integer isRead){
        try{
            //保存非机器人消息
            if(!message.getSessionId().equals(Constants.ImserverConfig.REBOT_SESSIONID)){
                MessageProto.Model  msg =  (MessageProto.Model)message.getBody();
                MessageBodyProto.MessageBody  msgConten =  MessageBodyProto.MessageBody.parseFrom(msg.getContent());
                MessageEntity  userMessage = new MessageEntity();
                userMessage.setSender(message.getSessionId());
                userMessage.setReceiver(message.getReSessionId());
                userMessage.setContent(msgConten.getContent());
                userMessage.setSendTime(msg.getTimeStamp());
                userMessage.setIsRead(isRead);
                return userMessage;
            }
        }catch(Exception e){
            throw new RuntimeException(e.getCause());
        }
        return null;
    }

    @Override
    public MessageWrapper getReConnectionStateMsg(String sessionId) {
        MessageProto.Model.Builder  result = MessageProto.Model.newBuilder();
        result.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        result.setSender(sessionId);//存入发送人sessionId
        result.setCmd(Constants.CmdType.RECON);
        return  new MessageWrapper(MessageWrapper.MessageProtocol.RECON, sessionId, null,result.build());
    }

    @Override
    public MessageWrapper getOfflineMsg( ) {
        MessageProto.Model.Builder  result = MessageProto.Model.newBuilder();
        result.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        result.setCmd(4);
        return new MessageWrapper(MessageWrapper.MessageProtocol.RECON, null, null,result.build());
    }
}
