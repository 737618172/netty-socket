package com.netty_websocket.im.model;

import lombok.Data;

@Data
public class Message {

    private int cmd ;//请求接口命令字  1绑定  2心跳   3上线   4下线  5消息 6重连
    private String sender;//发送人
    private String receiver;//接收人
    private String name ;//客户，客服名称
    private int msgtype ;//请求1，应答2，通知3，响应4  format
    private int flag = 8;//1 rsa加密 2aes加密
    private String platform ;//mobile-ios mobile-android pc-windows pc-mac
    private String platformVersion ;//客户端版本号
    private String token ;//客户端凭证
    //     string appKey = 12;//客户端key
    private String timeStamp ;//时间戳
    //     string sign = 14;//签名
    private byte content ;//请求数据
    private int utype  ;//用户类型
}
