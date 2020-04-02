/**
 ***************************************************************************************
 *  @Author     1044053532@qq.com   
 *  @License    http://www.apache.org/licenses/LICENSE-2.0
 ***************************************************************************************
 */
package com.example.nettyspringboot.im.service;

import com.example.nettyspringboot.im.model.MessageProto;
import com.example.nettyspringboot.im.model.MessageWrapper;

public interface MessageProxy {

    MessageWrapper convertToMessageWrapper(String sessionId, MessageProto.Model message);
}

