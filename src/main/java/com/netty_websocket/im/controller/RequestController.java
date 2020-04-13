package com.netty_websocket.im.controller;

import com.netty_websocket.im.Constants;
import com.netty_websocket.im.model.MessageEntity;
import com.netty_websocket.im.model.Session;
import com.netty_websocket.im.service.MessageService;
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
        for(Cookie c:cookies){
            if("token".equals(c.getName())){
                flag = true;
                c.setMaxAge(24 * 60 * 60 * 3);
                break;
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
//    3B566D22336B8C579E590A753D2BBD77
//    A13830AE9A4BFBF2B089011255C2D4DE
//    3414AE4B7F231F0DA0B61A22845A0CB7


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
        System.out.println("hisMessage");
        return messageService.queryHisMessage(sessionId);
    }

    @RequestMapping(value = "/redisSet",method = RequestMethod.GET)
    @ResponseBody
    public String redisSet(String sessionId){
        redisTemplate.opsForValue().set(sessionId,"123");
        return "ok";
    }
    @RequestMapping(value = "/redisGet",method = RequestMethod.GET)
    @ResponseBody
    public Object redisGet(String sessionId){
        return  redisTemplate.opsForValue().get(sessionId);
    }

    @RequestMapping(value = "/redisGetKeys",method = RequestMethod.GET)
    @ResponseBody
    public String redisGetKeys(String pattern){
        Set<String> keys = redisTemplate.keys(pattern);
        for(Object k:keys){
            System.out.println(k + "====" + redisTemplate.opsForValue().get(k));
        }
        return "ok";
    }

    @RequestMapping(value = "/redisHash",method = RequestMethod.GET)
    @ResponseBody
    public String redisHash(String sessionId){
        redisTemplate.opsForHash().put(1089,"server1","1server");
        redisTemplate.opsForHash().put(1089,"server2","2server");
        redisTemplate.opsForHash().put(1089,"server3","3server");
        return "ok";
    }

    @RequestMapping(value = "/redisHashG",method = RequestMethod.GET)
    @ResponseBody
    public String redisHashG(String sessionId){
        Set keys = redisTemplate.opsForHash().keys(1089);
        for(Object k :keys){
            System.out.println(k + "====" + redisTemplate.opsForHash().get(1089,k));
        }
        return "ok";
    }

    @RequestMapping(value = "/redis",method = RequestMethod.GET)
    @ResponseBody
    public Object redis(){
        Session session = new Session(null);
        session.setAccount(UUID.randomUUID().toString());
        redisTemplate.opsForValue().set(Constants.ImserverConfig.CUSTOMER_SESSION_PRE + session.getAccount(),session,5, TimeUnit.DAYS);

        return redisTemplate.opsForValue().get(Constants.ImserverConfig.CUSTOMER_SESSION_PRE + session.getAccount());
    }
}
