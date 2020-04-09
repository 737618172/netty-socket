package com.netty_websocket.im.model;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Data;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

@Data
public class Session implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8269505210699191257L;
    private Channel session;

    private Channel tSession;
    private int source;//来源 用于区分是websocket\socket\dwr
    private String deviceId;//客户端ID  (设备号码+应用包名),ios为devicetoken
    private String host;//session绑定的服务器IP
    private String account;//session绑定的账号
    private String platform;//终端类型
    private String platformVersion;//终端版本号
    private String appKey;//客户端key
    private Long bindTime;//登录时间
    private Long updateTime;//更新时间
    private String sign;//签名
    private Double longitude;//经度
    private Double latitude;//维度
    private String location;//位置
    private int status;// 状态
    private int count;


    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
        setAttribute("updateTime", updateTime);
    }


    public Session(Channel session) {
        this.session = session;
    }

    public Session() {

    }

    public void setLongitude(Double longitude) {
        setAttribute("longitude", longitude);
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        setAttribute("latitude", latitude);
        this.latitude = latitude;
    }

    public void setLocation(String location) {
        setAttribute("location", location);
        this.location = location;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;

        setAttribute("deviceId", deviceId);
    }

    public void setBindTime(Long bindTime) {
        this.bindTime = bindTime;
        setAttribute("bindTime", bindTime);
    }

    public void setHost(String host) {
        this.host = host;

        setAttribute("host", host);
    }

    public void setStatus(int status) {
        this.status = status;
        setAttribute("status", status);
    }

    public void setPlatform(String platform) {
        this.platform = platform;
        setAttribute("platform", platform);
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
        setAttribute("platformVersion", platformVersion);
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
        setAttribute("appKey", appKey);
    }

    public void setSign(String sign) {
        this.sign = sign;
        setAttribute("sign", sign);
    }

    public void setSource(int source) {
        this.source = source;
        setAttribute("source", source);
    }

    public void setAttribute(String key, Object value) {
        if (session != null)
            session.attr(AttributeKey.valueOf(key)).set(value);
    }


    public boolean containsAttribute(String key) {
        if (session != null)
            return session.hasAttr(AttributeKey.valueOf(key));
        return false;
    }

    public Object getAttribute(String key) {
        if (session != null)
            return session.attr(AttributeKey.valueOf(key)).get();
        return null;
    }

    public void removeAttribute(String key) {
        if (session != null)
            session.attr(AttributeKey.valueOf(key)).set(null);
        ;
    }

    public SocketAddress getRemoteAddress() {
        if (session != null)
            return session.remoteAddress();
        return null;
    }

    public boolean write(Object msg) {
        if (session != null ) {
            return session.writeAndFlush(msg).awaitUninterruptibly(5000);
        }
        return false;
    }


    public boolean isLocalhost() {

        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            return ip.equals(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;

    }


//	public void close() {
//		if(session!=null){
//			session.close();
//		}else if(dwrsession!=null){
//			dwrsession.invalidate();
//		}
//	}

//	public void closeAll() {
//		close();
//		for (String key : sessions.keySet()) {
//			Session  session = sessions.get(key);
//			if(session!=null){
//				session.close();
//				sessions.remove(key);
//			}
//		}
//	}

//	public void close(String nid) {
//		if(getNid().equals(nid)){
//			close();
//		}else{
//			Session  session = sessions.get(nid);
//			sessions.remove(nid);
//			session.close();
//		}
//	}

    public boolean equals(Object o) {

        if (o instanceof Session) {
            return hashCode() == o.hashCode();
        }
        return false;
    }

    public boolean fromOtherDevice(Object o) {

        if (o instanceof Session) {

            Session t = (Session) o;
            if (t.deviceId != null && deviceId != null) {
                return !t.deviceId.equals(deviceId);
            }
        }
        return false;
    }

    public boolean fromCurrentDevice(Object o) {

        return !fromOtherDevice(o);
    }


//	public String  toString(){
//		return  JSON.toJSONString(Session.this);
//	}


}