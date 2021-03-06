package com.netty_websocket.im.repository;

import com.netty_websocket.im.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.lang.annotation.Native;
import java.util.List;

@Repository
public interface MessageRepository  extends JpaRepository<MessageEntity,Long> {

    @Query("SELECT a from MessageEntity a where a.isRead = 2 and (a.sender = ?1 or a.receiver= ?1 )")
    List<MessageEntity> getOffLineMsg(String sender );

    @Query("SELECT a from MessageEntity a where a.sender = ?1 or a.receiver= ?1 order by a.sendTime")
    List<MessageEntity> getHistoryMsg(String sender);

}
