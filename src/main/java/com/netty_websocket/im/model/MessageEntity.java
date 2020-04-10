package com.netty_websocket.im.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
public class MessageEntity {

    //消息Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //发送者
    private String sender;
    //接收者
    private String receiver;
    //发送人名称
    private String senderName;
    //发送时间
    private String sendTime;
    //消息类型
    private String content;
    //1文本  2图片  3文件
    private Integer contentType;

    private Integer isRead;
}
