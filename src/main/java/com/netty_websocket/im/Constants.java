package com.netty_websocket.im;

import com.googlecode.protobuf.format.JsonFormat;
import io.netty.util.AttributeKey;

public class Constants {

	public static interface WebSite{
		public static final int SUCCESS = 0;
		public static final int ERROR = -1;
	}

	public static interface NotifyConfig{
		public static final int NOTIFY_SUCCESS = 1;
	    public static final int NOTIFY_FAILURE = 0;
	    public static final int NOTIFY_NO_SESSION = 2;
	}



    public static interface ImserverConfig{
    	//连接空闲时间
      	public static final int READ_IDLE_TIME = 30;//秒
      	//发送心跳包循环时间
      	public static final int WRITE_IDLE_TIME = 25;//秒
        //心跳响应 超时时间
      	public static final int PING_TIME_OUT = 70; //秒   需大于空闲时间

        // 最大协议包长度
        public static final int MAX_FRAME_LENGTH = 1024 * 10; // 10k
        //
        public static final int MAX_AGGREGATED_CONTENT_LENGTH = 65536;

        public static final String REBOT_SESSIONID="0";//机器人SessionID

        public static final int WEBSOCKET = 1;//websocket标识

        public static final int SOCKET =0;//socket标识

		public static final String CUSTOMER_SESSION_PRE =  "CSESSION";
    }

    public static interface SessionConfig{
    	 public static final String SESSION_KEY= "account" ;
    	 public static final AttributeKey<String> SERVER_SESSION_ID = AttributeKey.valueOf(SESSION_KEY);
    	 public static final AttributeKey SERVER_SESSION_HEARBEAT = AttributeKey.valueOf("heartbeat");
		public static final String SESSION_FROM = "sessionFrom";
		public static final AttributeKey<String> ATTR_SESSION_FROM = AttributeKey.valueOf(SESSION_FROM);
    }

    public static interface ProtobufType{
    	 byte SEND = 1; //请求
    	 byte RECEIVE = 2; //接收
    	 byte NOTIFY = 3; //通知
    	 byte REPLY = 4; //回复
	}

	public static interface UserType{
		byte CUSTOMER = 1; //客户
		byte SERVER = 2; //客服
	}

    public static interface CmdType{
	   	 byte BIND = 1; //绑定
	   	 byte HEARTBEAT = 2; //心跳
	   	 byte ONLINE = 3; //上线
	   	 byte OFFLINE = 4; //下线
	   	 byte MESSAGE = 5; //消息
	   	 byte RECON = 6; //重连
	}

	public static void main(String[] args) {
		System.out.println(Runtime.getRuntime().availableProcessors());
	}
}
