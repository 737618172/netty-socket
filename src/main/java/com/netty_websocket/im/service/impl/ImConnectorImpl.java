package com.netty_websocket.im.service.impl;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import com.netty_websocket.im.service.ImConnector;
import com.netty_websocket.im.service.MessageProxy;
import com.netty_websocket.im.model.Session;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ImConnectorImpl implements ImConnector {
    private final static Logger log = LoggerFactory.getLogger(ImConnectorImpl.class);

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
            MessageProto.Model body = (MessageProto.Model) wrapper.getBody();
            int utype = body.getUtype();
            Session responseSession = sessionManager.getSession(wrapper.getReSessionId(),utype == 1 ? 2:1);
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
            MessageProto.Model body = (MessageProto.Model) wrapper.getBody();
            int utype = body.getUtype();
            ///取得接收人 给接收人写入消息
            Session responseSession = sessionManager.getSession(wrapper.getReSessionId(),utype);
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
            MessageProto.Model  msg =  (MessageProto.Model)wrapper.getBody();
            int utype = msg.getUtype();
            Session session = null;
            if(1 == utype){
                session = (Session) redisTemplate.opsForValue().get(Constants.ImserverConfig.CUSTOMER_SESSION_PRE + sessionId);
            }else if(2 == utype){
                session = sessionManager.getSession(sessionId,utype);
            }

            //当sessionID存在或者相等  视为同一用户重新连接
            if (null != session) {
                Session session1 = sessionManager.switchSession(session, ctx,wrapper);
                log.info("connector reconnect sessionId -> " + sessionId + ", ctx -> " + ctx.toString());
                pushMessage(session1.getAccount(),proxy.getReConnectionStateMsg(session1.getAccount()));
            } else {
                log.info("connector connect sessionId -> " + sessionId +  ", ctx -> " + ctx.toString());
                sessionManager.createSession(wrapper, ctx);
                setChannelSessionId(ctx, sessionId,utype);
                log.info("create channel attr sessionId " + sessionId + " successful, ctx -> " + ctx.toString());
            }

        } catch (Exception e) {
            log.error("connector connect  Exception.", e);
        }
    }

    @Override
    public String[] getChannelSessionId(ChannelHandlerContext ctx) {
        String sessionId = ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
        String from = ctx.channel().attr(Constants.SessionConfig.ATTR_SESSION_FROM).get();
        return  new String[]{sessionId,from};
    }

    @Override
    public void close(ChannelHandlerContext hander) {
        String[] channelSessionId = getChannelSessionId(hander);
        String sessionId = channelSessionId[0];
        String uType = channelSessionId[1];

        try {
            Session session = sessionManager.getSession(sessionId,Integer.valueOf(uType));
            if (session != null) {
//                if(session.getServer() != null){
//                    sessionManager.removeSession(sessionId,1);
//                }
                if("1".equals(uType)){
                    session.setSession(null);
                    redisTemplate.opsForValue().set(Constants.ImserverConfig.CUSTOMER_SESSION_PRE,session,5, TimeUnit.DAYS);
                }else if("2".equals(uType)){
                    sessionManager.removeSession(sessionId,Integer.valueOf(uType));
                }

                log.info("connector close sessionId -> " + sessionId + " success " );
            }
        } catch (Exception e) {
            log.error("connector close sessionId -->"+sessionId+"  Exception.", e);
            throw new RuntimeException(e.getCause());
        }
    }

    private void setChannelSessionId(ChannelHandlerContext ctx, String sessionId,Integer uType) {
        ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).set(sessionId);
        ctx.channel().attr(Constants.SessionConfig.ATTR_SESSION_FROM).set(""+uType);
    }

}
