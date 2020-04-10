package com.netty_websocket.im.service;


import com.netty_websocket.im.model.MessageEntity;
import com.netty_websocket.im.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public List<MessageEntity> queryOffLineMessage(String sessionId){
        return  messageRepository.getOffLineMsg(sessionId);
    }

    public List<MessageEntity> queryHisMessage(String sessionId){
        return  messageRepository.getHistoryMsg(sessionId);
    }
}
