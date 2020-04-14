package com.netty_websocket.im.service.impl;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.ChatWindow;
import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;
import com.netty_websocket.im.model.Session;
import com.netty_websocket.im.repository.ChatWindowRepository;
import com.netty_websocket.im.service.MessageProxy;
import com.netty_websocket.im.service.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SessionManagerImpl implements SessionManager {

    private final static Logger log = LoggerFactory.getLogger(SessionManagerImpl.class);
    @Autowired
    private MessageProxy proxy;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ChatWindowRepository chatWindowRepository;

    /**
     * sessions
     */
    public static Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();


    //    server session
    public static Map<String, Session> serverSessions = new ConcurrentHashMap<String, Session>();


    @Override
    public synchronized void addSession(Session session, int type) {
        if (null == session) {
            return;
        }
        if (1 == type) {
            redisTemplate.opsForValue().set(Constants.ImserverConfig.CUSTOMER_SESSION_PRE, session, 5, TimeUnit.DAYS);
        } else if (2 == type) {
            serverSessions.put(session.getAccount(), session);
            log.debug("put a session " + session.getAccount() + " to serverSessions!");
            log.debug("serverSessions size " + serverSessions.size());
        }
    }

    @Override
    public synchronized void updateSession(Session session, int type) {
        session.setUpdateTime(System.currentTimeMillis());
        if (1 == type) {
            redisTemplate.opsForValue().set(Constants.ImserverConfig.CUSTOMER_SESSION_PRE, session, 5, TimeUnit.DAYS);
//            sessions.put(session.getAccount(), session);
        } else if (2 == type) {
            serverSessions.put(session.getAccount(), session);
//            redisTemplate.opsForHash().put(1089+"server",session.getAccount(),session);
//            redisTemplate.expire(1089+"server",12,TimeUnit.HOURS);
        }
    }

    @Override
    public synchronized void updateSessionCustomer(Session session, Session customerSession) {
        customerSession.setServer(session.getSession());
        session.setUpdateTime(System.currentTimeMillis());
        session.setCount(session.getCount() + 1);

//        List<Channel> customers = session.getCustomers();
//        if(null == customers){
//            customers = new ArrayList<>();
//            session.setCustomers(customers);
//        }
//        customers.add(customerSession.getSession());

//        redisTemplate.opsForValue().set(Constants.ImserverConfig.CUSTOMER_SESSION_PRE + session.getAccount(),session,5, TimeUnit.DAYS);
//        redisTemplate.opsForHash().put(1089,session.getAccount(),session);
        redisTemplate.opsForValue().set(Constants.ImserverConfig.CUSTOMER_SESSION_PRE + customerSession.getAccount(), customerSession, 5, TimeUnit.DAYS);
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
    public synchronized void removeSession(String sessionId, Integer uType) {
        try {
            Session session = getSession(sessionId, uType);
            if (session != null) {
                Channel channel = channels.get(sessionId);
                if(null != channel){
                    channel.close();
                }
                //判断没有其它session后 从SessionManager里面移除
                if (1 == uType) {
//                    sessions.remove(sessionId);
                    Session s = (Session) redisTemplate.opsForValue().get(Constants.ImserverConfig.CUSTOMER_SESSION_PRE + sessionId);
                    if (null != s) {
                        s.setSession(null);
                        redisTemplate.opsForValue().set(Constants.ImserverConfig.CUSTOMER_SESSION_PRE + sessionId, session, 5, TimeUnit.DAYS);
                    }
//                    MessageProto.Model model = proxy.getOffLineStateMsg(sessionId);
                } else if (2 == uType) {
                    serverSessions.remove(sessionId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {


        }
        log.info("remove the session " + sessionId + " from sessions!");
    }

    @Override
    public Session getSession(String sessionId, Integer uType) {
        Session session = null;
        if (1 == uType) {
            session = (Session) redisTemplate.opsForValue().get(Constants.ImserverConfig.CUSTOMER_SESSION_PRE + sessionId);
        } else if (2 == uType) {
            session = serverSessions.get(sessionId);
        }
        return session;
    }
//    @Override
//    public Session[] getSessions() {
//        return sessions.values().toArray(new Session[0]);
//    }
//    @Override
//    public Set<String> getSessionKeys() {
//        return sessions.keySet();
//    }
//    @Override
//    public int getSessionCount() {
//        return sessions.size();
//    }


    @Override
    public Session createSession(MessageWrapper wrapper, ChannelHandlerContext ctx) {
        String sessionId = wrapper.getSessionId();
        channels.put(sessionId,ctx.channel());
        MessageProto.Model body = (MessageProto.Model) wrapper.getBody();
        int utype = body.getUtype();

        Session session = setSessionContent(ctx, wrapper, sessionId);
        session.setSession(sessionId);
        if (1 == utype) {//为客户分配客服
            disCustomer(session);
        }
        addSession(session, utype);
        return session;
    }

    /**
     * 设置session内容
     *
     * @param ctx
     * @param wrapper
     * @param sessionId
     * @return
     */
    private Session setSessionContent(ChannelHandlerContext ctx, MessageWrapper wrapper, String sessionId) {
        log.info("create new session " + sessionId + ", ctx -> " + ctx.toString());
        MessageProto.Model model = (MessageProto.Model) wrapper.getBody();
        Session session = new Session(sessionId);
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


//	@Override
//	public boolean exist(String sessionId) {
//        Session session = getSession(sessionId);
//        return session != null ;
//	}

    @Override
    public Session switchSession(Session session, ChannelHandlerContext ctx, MessageWrapper wrapper) {
        MessageProto.Model  msg =  (MessageProto.Model)wrapper.getBody();
        if (StringUtils.isNotEmpty(session.getSession())) {
            Channel channel = channels.get(session.getSession());
            if (null != channel) {
                channel.writeAndFlush(proxy.getOfflineMsg());
                channel.close();
            }
        }
        channels.put(session.getSession(), ctx.channel());
        if(msg.getUtype()==1){
            if(StringUtils.isEmpty(session.getServer()) || null == channels.get(session.getServer())){
                disCustomer(session);
            }else {
                MessageProto.Model serverConnMsg = proxy.getServerConnMsg(session.getServer());
                writeMsg(session,serverConnMsg);
            }
        }
        return session;
    }

    public void writeMsg(Session session,MessageProto.Model msg){
        if(null != session && StringUtils.isNotEmpty(session.getSession())){
            Channel channel = channels.get(session.getSession());
            if(null != channel){
                channel.writeAndFlush(msg);
            }
        }
    }

    public void disCustomer(Session session){
        if (serverSessions.size() > 0) {
            List<Session> collect = serverSessions.values().stream().sorted(Comparator.comparing(Session::getCount)).collect(Collectors.toList());
            Session session2 = collect.get(0);
            updateSessionCustomer(session2, session);

            ChatWindow chatWindow = new ChatWindow();
            chatWindow.setCustomerSession(session.getSession());
            chatWindow.setServerSession(session2.getAccount());
//                chatWindow.setUserId();
//                chatWindow.setSId();
            System.out.println("窗口的Id====" + chatWindow.getId());
            chatWindowRepository.save(chatWindow);

            MessageProto.Model onLineStateMsg = proxy.getCustomerConnMsg(session.getAccount());
            writeMsg(session2,onLineStateMsg);
            MessageProto.Model serverConnMsg = proxy.getServerConnMsg(session2.getAccount());
            writeMsg(session,serverConnMsg);

        } else {
            //提示没有空闲客服 TODO
        }
    }
}