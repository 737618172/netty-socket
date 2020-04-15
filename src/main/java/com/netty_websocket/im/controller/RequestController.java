package com.netty_websocket.im.controller;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.MessageEntity;
import com.netty_websocket.im.model.Session;
import com.netty_websocket.im.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import java.util.concurrent.TimeUnit;

@Controller
public class RequestController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/inPage")
    public ModelAndView inPage(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView();
        Cookie[] cookies = request.getCookies();
        boolean flag = false;
        if(null != cookies){
            for(Cookie c:cookies){
                if("token".equals(c.getName())){
                    flag = true;
                    c.setMaxAge(24 * 60 * 60 * 3);
                    break;
                }
            }
        }
        if(!flag){
            Cookie cookie = new Cookie("token",UUID.randomUUID().toString());
            cookie.setMaxAge(24 * 60 * 60 * 3);
            response.addCookie(cookie);
        }
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
        System.out.println("offLineMessage");
        return messageService.queryOffLineMessage(sessionId);
    }

    @RequestMapping(value = "/hisMessage",method = RequestMethod.POST)
    @ResponseBody
    public List<MessageEntity> hisMessage(String sessionId){

        if(StringUtils.isEmpty(sessionId)){
            return null;
        }
        System.out.println("hisMessage and session Id ==" + sessionId);
        return messageService.queryHisMessage(sessionId);
    }

//    @RequestMapping(value = "/customer",method = RequestMethod.GET)
//    @ResponseBody
//    public Object customer(String sessionId){
//        redisTemplate.opsForValue().get();
//        System.out.println("hisMessage");
//        return messageService.queryHisMessage(sessionId);
//    }
}
