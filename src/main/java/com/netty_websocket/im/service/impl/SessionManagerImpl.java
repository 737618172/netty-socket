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
import com.netty_websocket.im.service.MessageProxy;
import com.netty_websocket.im.service.Session;
import com.netty_websocket.im.service.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManagerImpl implements SessionManager {

    private final static Logger log = LoggerFactory.getLogger(SessionManagerImpl.class);
    private MessageProxy proxy;
    
    public void setProxy(MessageProxy proxy) {
		this.proxy = proxy;
	}
    /**
     * The set of currently active Sessions for this Manager, keyed by session
     * identifier.
     */
    protected Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    protected Map<Long, Map<String,Session>> serverSessions = new ConcurrentHashMap<Long, Map<String,Session>>();



    public synchronized void addSession(Session session) {
        if (null == session) {
            return;
        } 
        sessions.put(session.getAccount(), session);
//        if(session.getSource()!= Constants.ImserverConfig.DWR){
//        	ImChannelGroup.add(session.getSession());
//        }
        //全员发送上线消息
//        MessageProto.Model model = proxy.getOnLineStateMsg(session.getAccount());
//        ImChannelGroup.broadcast(model);
        log.debug("put a session " + session.getAccount() + " to sessions!");
        log.debug("session size " + sessions.size() );
    }

    public synchronized void updateSession(Session session) {
        session.setUpdateTime(System.currentTimeMillis());
        sessions.put(session.getAccount(), session);
    }

    /**
     * Remove this Session from the active Sessions for this Manager.
     */
    public synchronized void removeSession(String sessionId) {
//    	try{
//    		Session session = getSession(sessionId);
//    		if(session!=null){
//    			session.closeAll();
//				sessions.remove(sessionId);
//				MessageProto.Model model = proxy.getOffLineStateMsg(sessionId);
//				ImChannelGroup.broadcast(model);
//				DwrUtil.sedMessageToAll(model);
//    		}
//    	}catch(Exception e){
//
//    	}
//    	log.debug("session size " + sessions.size() );
//    	log.info("system remove the session " + sessionId + " from sessions!");
    }
    
    
    public synchronized void removeSession(String sessionId,String nid) {
//    	try{
//    		Session session = getSession(sessionId);
//    		if(session!=null){
//    			int source = session.getSource();
//    			if(source==Constants.ImserverConfig.WEBSOCKET || source==Constants.ImserverConfig.DWR){
//    				session.close(nid);
//    				//判断没有其它session后 从SessionManager里面移除
//    				if(session.otherSessionSize()<1){
//    					sessions.remove(sessionId);
//    					MessageProto.Model model = proxy.getOffLineStateMsg(sessionId);
//    					ImChannelGroup.broadcast(model);
//    					//dwr全员消息广播
//    					DwrUtil.sedMessageToAll(model);
//    				}
//    			} else{
//    				session.close();
//    				sessions.remove(sessionId);
//    				MessageProto.Model model = proxy.getOffLineStateMsg(sessionId);
//    				ImChannelGroup.broadcast(model);
//    				DwrUtil.sedMessageToAll(model);
//    			}
//    		}
//    	}catch(Exception e){
//
//    	}finally{
//
//
//    	}
//        log.info("remove the session " + sessionId + " from sessions!");
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public Session[] getSessions() {
        return sessions.values().toArray(new Session[0]);
    }

    public Set<String> getSessionKeys() {
        return sessions.keySet();
    }

    public int getSessionCount() {
        return sessions.size();
    }


    @Override
    public  Session createSession(MessageWrapper wrapper, ChannelHandlerContext ctx) {
    	String sessionId = wrapper.getSessionId();
        Session session = sessions.get(sessionId);
        if (session != null) {
        	log.info("session " + sessionId + " exist!");
            //用于解决websocket多开页面session被踢下线的问题
            Session  newsession = setSessionContent(ctx,wrapper,sessionId);
            updateSession(session);
//            ImChannelGroup.add(newsession.getSession());
            log.info("session " + sessionId + " update!");
            return newsession;
        }

        session = setSessionContent(ctx,wrapper,sessionId);
        addSession(session);
        return session;
    }
    
    
    /**
     * 设置session内容
     * @param ctx
     * @param wrapper
     * @param sessionId
     * @return
     */
    private Session  setSessionContent(ChannelHandlerContext ctx, MessageWrapper wrapper, String sessionId){
    	 log.info("create new session " + sessionId + ", ctx -> " + ctx.toString());
    	  MessageProto.Model model = (MessageProto.Model)wrapper.getBody();
    	  Session session = new Session(ctx.channel());
          session.setAccount(sessionId);
//          session.setSource(wrapper.getSource());
          session.setAppKey(model.getAppKey());
          session.setDeviceId(model.getDeviceId());
          session.setPlatform(model.getPlatform());
          session.setPlatformVersion(model.getPlatformVersion());
          session.setSign(model.getSign());
          session.setBindTime(System.currentTimeMillis());
          session.setUpdateTime(session.getBindTime());
          log.info("create new session " + sessionId + " successful!");
          return session;
    }


	@Override
	public boolean exist(String sessionId) {
        Session session = getSession(sessionId);
        return session != null ? true : false;
	}

    public void serverSession(Session session) {
        if(null != session){
            Map<String, Session> sessions = serverSessions.get(1089L);
            if(null == sessions){
                Map<String, Session> m = new HashMap<>();
                m.put(session.getAccount(),session);
                serverSessions.put(1089L,m);
            }
        }
    }

    public Channel getServer() {
        Map<String, Session> servers = serverSessions.get(1089L);
        int i1 = new Random().nextInt(servers.size());
        for(int i = 0;i<servers.size();i++){
            if(i == i1){
                Set<String> strings = servers.keySet();
                return
            }
        }
    }
}
