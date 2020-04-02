/**
 ***************************************************************************************
 *  @Author     1044053532@qq.com   
 *  @License    http://www.apache.org/licenses/LICENSE-2.0
 ***************************************************************************************
 */
package com.netty_websocket.im.service;

import com.netty_websocket.im.model.MessageProto;
import com.netty_websocket.im.model.MessageWrapper;

public interface MessageProxy {

    MessageWrapper convertToMessageWrapper(String sessionId, MessageProto.Model message);
}
