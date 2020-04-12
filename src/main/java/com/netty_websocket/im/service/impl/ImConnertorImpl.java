package com.netty_websocket.im.service.impl;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import com.netty_websocket.im.service.ImConnertor;
import com.netty_websocket.im.service.MessageProxy;
import com.netty_websocket.im.model.Session;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImConnertorImpl implements ImConnertor {
	private final static Logger log = LoggerFactory.getLogger(ImConnertorImpl.class);

	@Autowired
    private SessionManagerImpl sessionManager;

    @Autowired
    private MessageProxy proxy;

    @Autowired
    private RedisTemplate redisTemplate;

	@Override
	public void heartbeatToClient(ChannelHandlerContext hander, MessageWrapper wrapper) {
		//设置心跳响应时间
		hander.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(System.currentTimeMillis());
	}

	@Override
	public void pushMessage(MessageWrapper wrapper) throws RuntimeException {
        try {
            ///取得接收人 给接收人写入消息
            Session responseSession = sessionManager.getSession(wrapper.getReSessionId());
            if (responseSession != null ) {
                boolean result = responseSession.write(wrapper.getBody());
                if(result){
                    proxy.saveOnlineMessageToDB(wrapper);
                }else{
                    proxy.saveOfflineMessageToDB(wrapper);
                }
                return;
            }else{
                proxy.saveOfflineMessageToDB(wrapper);
            }
        } catch (Exception e) {
            log.error("connector send occur PushException.", e);

            throw new RuntimeException(e.getCause());
        }
    }


	@Override
	public void pushMessage(String sessionId,MessageWrapper wrapper) throws RuntimeException{
		//判断是不是无效用户回复
//		if(!sessionId.equals(Constants.ImserverConfig.REBOT_SESSIONID)){//判断非机器人回复时验证
//			Session session = sessionManager.getSession(sessionId);
//	        if (session == null) {
//	        	 throw new RuntimeException(String.format("session %s is not exist.", sessionId));
//	        }
//		}
	   try {
	    	///取得接收人 给接收人写入消息
	    	Session responseSession = sessionManager.getSession(wrapper.getReSessionId());
	  		if (responseSession != null ) {
	  			boolean result = responseSession.write(wrapper.getBody());
	  			if(result){
	  				proxy.saveOnlineMessageToDB(wrapper);
	  			}else{
	  				proxy.saveOfflineMessageToDB(wrapper);
	  			}
	  			return;
	  		}else{
	  			proxy.saveOfflineMessageToDB(wrapper);
	  		}
	    } catch (Exception e) {
	    	log.error("connector send occur PushException.", e);

	        throw new RuntimeException(e.getCause());
	    }

	}

    @Override
    public void connect(ChannelHandlerContext ctx, MessageWrapper wrapper) {
        try {
        	  String sessionId = wrapper.getSessionId();
//        	  String channelSession = getChannelSessionId(ctx);
            Session session = (Session) redisTemplate.opsForValue().get("session" + sessionId);

            //当sessionID存在或者相等  视为同一用户重新连接
              if (null != session) {
                  Session session1 = sessionManager.switchSession(session, ctx);
                  log.info("connector reconnect sessionId -> " + sessionId + ", ctx -> " + ctx.toString());
//                  pushMessage(proxy.getReConnectionStateMsg(s));
              } else {
                  log.info("connector connect sessionId -> " + sessionId + ", sessionId0 -> " + channelSession + ", ctx -> " + ctx.toString());
                  sessionManager.createSession(wrapper, ctx);
                  setChannelSessionId(ctx, sessionId);
                  log.info("create channel attr sessionId " + sessionId + " successful, ctx -> " + ctx.toString());
              }

        } catch (Exception e) {
        	log.error("connector connect  Exception.", e);
        }
    }

	@Override
    public String getChannelSessionId(ChannelHandlerContext ctx) {
        return ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
    }

    @Override
    public void close(ChannelHandlerContext hander) {
        String sessionId = getChannelSessionId(hander);
        try {
            Session session = sessionManager.getSession(sessionId);
            if (session != null) {
                if(session.getServer() != null){
                    sessionManager.removeSession(sessionId,1);
                }

                log.info("connector close sessionId -> " + sessionId + " success " );
            }
        } catch (Exception e) {
            log.error("connector close sessionId -->"+sessionId+"  Exception.", e);
            throw new RuntimeException(e.getCause());
        }
    }

    private void setChannelSessionId(ChannelHandlerContext ctx, String sessionId) {
        ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).set(sessionId);
    }


}
