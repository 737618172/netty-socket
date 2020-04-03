/**
 ***************************************************************************************
 *  @Author     1044053532@qq.com
 *  @License    http://www.apache.org/licenses/LICENSE-2.0
 ***************************************************************************************
 */
package com.netty_websocket.im.service.impl;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import com.netty_websocket.im.service.ImConnertor;
import com.netty_websocket.im.service.MessageProxy;
import com.netty_websocket.im.service.Session;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImConnertorImpl implements ImConnertor {
	private final static Logger log = LoggerFactory.getLogger(ImConnertorImpl.class);

	@Autowired
    private SessionManagerImpl sessionManager;

    @Autowired
    private MessageProxy proxy;

	@Override
	public void heartbeatToClient(ChannelHandlerContext hander, MessageWrapper wrapper) {
		//设置心跳响应时间
		hander.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(System.currentTimeMillis());
	}

	@Override
	public void pushMessage(MessageWrapper wrapper) throws RuntimeException {
//        try {
//        	//sessionManager.send(wrapper.getSessionId(), wrapper.getBody());
//        	Session session = sessionManager.getSession(wrapper.getSessionId());
//      		/*
//      		 * 服务器集群时，可以在此
//      		 * 判断当前session是否连接于本台服务器，如果是，继续往下走，如果不是，将此消息发往当前session连接的服务器并 return
//      		 * if(session!=null&&!session.isLocalhost()){//判断当前session是否连接于本台服务器，如不是
//      		 * //发往目标服务器处理
//      		 * return;
//      		 * }
//      		 */
//      		if (session != null) {
//      			boolean result = session.write(wrapper.getBody());
//      			return ;
//      		}
//        } catch (Exception e) {
//        	log.error("connector pushMessage  Exception.", e);
//            throw new RuntimeException(e.getCause());
//        }
    }


	@Override
	public void pushMessage(String sessionId,MessageWrapper wrapper) throws RuntimeException{
//		//判断是不是无效用户回复
//		if(!sessionId.equals(Constants.ImserverConfig.REBOT_SESSIONID)){//判断非机器人回复时验证
//			Session session = sessionManager.getSession(sessionId);
//	        if (session == null) {
//	        	 throw new RuntimeException(String.format("session %s is not exist.", sessionId));
//	        }
//		}
//	   try {
//	    	///取得接收人 给接收人写入消息
//	    	Session responseSession = sessionManager.getSession(wrapper.getReSessionId());
//	  		if (responseSession != null && responseSession.isConnected() ) {
//	  			boolean result = responseSession.write(wrapper.getBody());
//	  			if(result){
//	  				proxy.saveOnlineMessageToDB(wrapper);
//	  			}else{
//	  				proxy.saveOfflineMessageToDB(wrapper);
//	  			}
//	  			return;
//	  		}else{
//	  			proxy.saveOfflineMessageToDB(wrapper);
//	  		}
//	    } catch (PushException e) {
//	    	log.error("connector send occur PushException.", e);
//
//	        throw new RuntimeException(e.getCause());
//	    } catch (Exception e) {
//	    	log.error("connector send occur Exception.", e);
//	        throw new RuntimeException(e.getCause());
//	    }
//
	}

    @Override
    public void connect(ChannelHandlerContext ctx, MessageWrapper wrapper) {
        try {
        	  String sessionId = wrapper.getSessionId();
        	  String channelSession = getChannelSessionId(ctx);

        	  //当sessionID存在或者相等  视为同一用户重新连接
              if (StringUtils.isNotEmpty(channelSession) || sessionId.equals(channelSession)) {
                  log.info("connector reconnect sessionId -> " + sessionId + ", ctx -> " + ctx.toString());
//                  pushMessage(proxy.getReConnectionStateMsg(channelSession));
              } else {
                  log.info("connector connect sessionId -> " + sessionId + ", sessionId0 -> " + channelSession + ", ctx -> " + ctx.toString());
                  Session session = sessionManager.createSession(wrapper, ctx);
                  setChannelSessionId(ctx, sessionId);
                  log.info("create channel attr sessionId " + sessionId + " successful, ctx -> " + ctx.toString());

                  MessageProto.Model body = (MessageProto.Model) wrapper.getBody();
                  if(Constants.UserType.CUSTOMER == body.getUtype()){
                      session.setTSession(sessionManager.getServer());
                  }else if (Constants.UserType.SERVER == body.getUtype()){
                      sessionManager.serverSession(session);
                  }
              }

        } catch (Exception e) {
        	log.error("connector connect  Exception.", e);
        }
    }

	@Override
    public String getChannelSessionId(ChannelHandlerContext ctx) {
        return ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
    }

    private void setChannelSessionId(ChannelHandlerContext ctx, String sessionId) {
        ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).set(sessionId);
    }


}
