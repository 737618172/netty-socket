package com.netty_websocket.im.service;

import com.netty_websocket.im.model.MessageWrapper;
import com.netty_websocket.im.model.Session;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.Set;

public interface SessionManager {

    /**
     * 添加指定session
     *
     * @param session
     */
    void addSession(Session session,int type);

    void updateSession(Session session,int type);

    void updateSessionCustomer(Session session,Session customerSession);
 

    /**
     * 删除指定session
     *
     * @param sessionId
     */
     void removeSession(String sessionId);

    /**
     * 删除指定session
     * 
     *
     * @param sessionId
     * @param uType  is user type
     */
     void removeSession(String sessionId, Integer uType );

    /**
     * 根据指定sessionId获取session
     *
     * @param sessionId
     * @return
     */
    Session getSession(String sessionId,Integer uType);

    /**
     * 获取所有的session
     *
     * @return
     */
//    Session[] getSessions();

    /**
     * 获取所有的session的id集合
     *
     * @return
     */
//    Set<String> getSessionKeys();

    /**
     * 获取所有的session数目
     *
     * @return
     */
//    int getSessionCount();
 
    Session createSession(MessageWrapper wrapper, ChannelHandlerContext ctx);
    
//    boolean exist(String sessionId) ;

    Session switchSession(Session session, ChannelHandlerContext ctx,MessageWrapper wrapper);
}
