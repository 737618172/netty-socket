package com.netty_websocket.im.service;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ImChannelGroup {
	 
    private static final ChannelGroup CHANNELGROUP = new DefaultChannelGroup("ChannelGroup", GlobalEventExecutor.INSTANCE);


    public static void add(Channel channel) {
        CHANNELGROUP.add(channel);
    }
    public static void remove(Channel channel) {
        CHANNELGROUP.remove(channel);
    }
  
    /**
     * 广播 
     * @param msg
     * @return
     */
    public static ChannelGroupFuture broadcast(Object msg) {
        return CHANNELGROUP.writeAndFlush(msg);
    }
    /**
     * 广播
     * @param msg
     * @param matcher
     * @return
     */
    public static ChannelGroupFuture broadcast(Object msg, ChannelMatcher matcher) {
        return CHANNELGROUP.writeAndFlush(msg, matcher);
    }
     
    public static ChannelGroup flush() {
        return CHANNELGROUP.flush();
    }
    /**
     * 丢弃无用连接 
     * @param channel
     * @return
     */
    public static boolean discard(Channel channel) {
        return CHANNELGROUP.remove(channel);
    }
}