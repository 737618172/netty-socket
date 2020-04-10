package com.netty_websocket.im.controller;

import com.netty_websocket.im.model.MessageEntity;
import com.netty_websocket.im.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
public class RequestController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/inPage")
    public ModelAndView inPage(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        request.getSession().setAttribute("uuid", UUID.randomUUID().toString());
        modelAndView.setViewName("hello");
        return modelAndView;
    }


    @RequestMapping(value = "/chat")
    public ModelAndView chat(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("chat");
        return modelAndView;
    }

    @RequestMapping(value = "/offLineMessage",method = RequestMethod.POST)
    @ResponseBody
    public List<MessageEntity> offLineMessage(String sessionId){
        return messageService.queryOffLineMessage(sessionId);
    }

    @RequestMapping(value = "/hisMessage",method = RequestMethod.POST)
    @ResponseBody
    public List<MessageEntity> hisMessage(String sessionId){
        return messageService.queryHisMessage(sessionId);
    }

}
