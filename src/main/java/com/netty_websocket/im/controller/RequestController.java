package com.netty_websocket.im.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class RequestController {

    @RequestMapping(value = "/inPage")
    public ModelAndView inPage(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/WEB-INF/pages/hello.jsp");
        HttpSession session = request.getSession();
        return modelAndView;
    }


    @RequestMapping(value = "/chat")
    public ModelAndView chat(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/WEB-INF/pages/chat.jsp");
        HttpSession session = request.getSession();
        return modelAndView;
    }
}
