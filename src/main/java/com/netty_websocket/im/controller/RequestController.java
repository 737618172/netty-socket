package com.netty_websocket.im.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@RestController
public class RequestController {

    @GetMapping("/inPage")
    public void inPage(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession();
        String ses = UUID.randomUUID().toString();
        session.setAttribute("cid",ses );
        System.out.println(ses);
//        return "ok";
    }
}
