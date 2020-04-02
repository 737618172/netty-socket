package com.netty_websocket.im.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class RequestController {

    @PostMapping("/inPage")
    public void inPage(HttpServletRequest request, HttpServletResponse response){
        System.out.println("cookie请求");
        HttpSession session = request.getSession();
        System.out.println(session);
    }
}
