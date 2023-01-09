package com.northboat.bearchat.controller;

import com.northboat.bearchat.service.UserService;
import com.northboat.bearchat.utils.RedisUtil;
import com.northboat.bearchat.websocket.WebSocketServer;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;

@Controller
public class ChatController {
    private UserService userService;
    @Autowired
    public void setUserService(UserService userService){
        this.userService = userService;
    }

    @RequestMapping("/park")
    public String park(HttpSession session, Model model){
        int login = Objects.isNull(session.getAttribute("login")) ? 0 : 1;
        String name = (String) session.getAttribute("user");
        int count = WebSocketServer.getOnlineCount() + 1;

        //System.out.println(WebSocketServer.getWebSocketSet().size());

        model.addAttribute("login", login);
        model.addAttribute("name", name);
        model.addAttribute("count", count);
        model.addAttribute("room", "park");
        return "chat/park";
    }

    @RequestMapping("/pick")
    public String pick(HttpSession session, Model model){
        Integer login = (Integer) session.getAttribute("login");
        if(Objects.isNull(login) || login == 0){
            model.addAttribute("msg", "请先登录");
            return "user/login";
        }
        String account = (String) session.getAttribute("user");
        String room = userService.pick(account);
        // 若房间为空
        if(room.equals("null")){
            model.addAttribute("notfound", true);
            model.addAttribute("self", account);
            model.addAttribute("friend", "空气");
            model.addAttribute("room", room);
            return "chat/room";
        }
        model.addAttribute("notfound", false);
        List<String> names = userService.getRoom(room);
        model.addAttribute("self", names.get(0));
        model.addAttribute("friend", names.get(1));
        model.addAttribute("room", room);
        return "chat/room";
    }
}
