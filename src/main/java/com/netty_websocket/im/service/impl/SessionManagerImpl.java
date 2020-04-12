package com.netty_websocket.im.service.impl;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import com.netty_websocket.im.model.Session;
import com.netty_websocket.im.service.MessageProxy;
import com.netty_websocket.im.service.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SessionManagerImpl implements SessionManager {

    private final static Logger log = LoggerFactory.getLogger(SessionManagerImpl.class);
    @Autowired
    private MessageProxy proxy;

    @Autowired
    private RedisTemplate redisTemplate;
    
    /**
     * customer session
     */
    protected Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();


    //server session
    protected Map<String,Session> serverSessions = new ConcurrentHashMap<String,Session>();


    @Override
    public synchronized void addSession(Session session,int type) {
        if (null == session) {
            return;
        }
        if(1 == type){
            sessions.put(session.getAccount(), session);
            log.debug("put a session " + session.getAccount() + " to sessions!");
            log.debug("sessions size " + sessions.size() );
        }else if(2==type){
            serverSessions.put(session.getAccount(), session);
            log.debug("put a session " + session.getAccount() + " to serverSessions!");
            log.debug("serverSessions size " + serverSessions.size() );
        }
    }

    @Override
    public synchronized void updateSession(Session session,int type) {
        session.setUpdateTime(System.currentTimeMillis());
        if(1 == type){
            sessions.put(session.getAccount(), session);
        }else if(2 == type){
            serverSessions.put(session.getAccount(), session);
        }
    }

    @Override
    public synchronized void updateSessionCustomer(Session session, Session customerSession) {
        customerSession.setServer(session.getSession());
        session.setUpdateTime(System.currentTimeMillis());
        session.setCount(session.getCount() +1);
        List<Channel> customers = session.getCustomers();
        if(null == customers){
            customers = new ArrayList<>();
            session.setCustomers(customers);
        }
        customers.add(customerSession.getSession());
    }

    /**
     * Remove this Session from the active Sessions for this Manager.
     */
    @Override
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

    @Override
    public synchronized void removeSession(String sessionId,Integer uType ) {
    	try{
    		Session session = getSession(sessionId);
    		if(session!=null){
    			int source = session.getSource();
                session.close();
                //判断没有其它session后 从SessionManager里面移除
                if(1 == uType){
                    sessions.remove(sessionId);
//                    MessageProto.Model model = proxy.getOffLineStateMsg(sessionId);
                }else if(2 ==uType){
                    serverSessions.remove(sessionId);
                }
    		}
    	}catch(Exception e){
            e.printStackTrace();
    	}finally{


    	}
        log.info("remove the session " + sessionId + " from sessions!");
    }

    @Override
    public Session getSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if(null == session){
            session = serverSessions.get(sessionId);
        }
        return session;
    }
    @Override
    public Session[] getSessions() {
        return sessions.values().toArray(new Session[0]);
    }
    @Override
    public Set<String> getSessionKeys() {
        return sessions.keySet();
    }
    @Override
    public int getSessionCount() {
        return sessions.size();
    }


    @Override
    public  Session createSession(MessageWrapper wrapper, ChannelHandlerContext ctx) {
    	String sessionId = wrapper.getSessionId();
        Session session = sessions.get(sessionId);
        Session session1 = serverSessions.get(sessionId);
        MessageProto.Model body = (MessageProto.Model) wrapper.getBody();
        int utype = body.getUtype();
        if (session != null || session1 != null) {
        	log.info("session " + sessionId + " exist!");
            //用于解决websocket多开页面session被踢下线的问题
            Session  newsession = setSessionContent(ctx,wrapper,sessionId);
            updateSession(newsession,utype);
            log.info("session " + sessionId + " update!");
            return newsession;
        }

        session = setSessionContent(ctx,wrapper,sessionId);
        if(1 == utype){//为客户分配客服
            if(serverSessions.size()>0){
                List<Session> collect = serverSessions.values().stream().sorted(Comparator.comparing(Session::getCount)).collect(Collectors.toList());
                Session session2 = collect.get(0);
                updateSessionCustomer(session2,session);

                MessageProto.Model onLineStateMsg = proxy.getCustomerConnMsg(session.getAccount());
                session2.getSession().writeAndFlush(onLineStateMsg);
                MessageProto.Model serverConnMsg = proxy.getServerConnMsg(session2.getAccount());
                session.getSession().writeAndFlush(serverConnMsg);
                log.info("为客服分配客户"+ session.getAccount());
            }else{
                //提示没有空闲客服
            }
        }
        addSession(session,utype);
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
        return session != null ;
	}

    @Override
    public Session switchSession(Session session, ChannelHandlerContext ctx) {
        if(session.getSession() != null){
            session.getSession().writeAndFlush(proxy.getOfflineMsg());
//            session.setSession(ctx.channel());
        }
            session.setSession(ctx.channel());
        return session;
    }
}