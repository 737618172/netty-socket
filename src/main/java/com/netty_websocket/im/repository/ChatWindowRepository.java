package com.netty_websocket.im.repository;


import com.netty_websocket.im.model.ChatWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatWindowRepository extends JpaRepository<ChatWindow,Long> {

}
