package com.northboat.bearchat.controller;

import com.northboat.bearchat.pojo.Message;
import com.northboat.bearchat.pojo.Room;
import com.northboat.bearchat.pojo.User;
import com.northboat.bearchat.service.RoomService;
import com.northboat.bearchat.service.UserService;
import com.northboat.bearchat.websocket.WebSocketServer;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class ChatController {
    private UserService userService;
    @Autowired
    public void setUserService(UserService userService){
        this.userService = userService;
    }


    private RoomService roomService;
    @Autowired
    public void RoomService(RoomService roomService){
        this.roomService = roomService;
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
        return "chat/public";
    }


    @RequestMapping("/channel")
    public String rooms(HttpSession session, Model model){
        Integer login = (Integer) session.getAttribute("login");
        if(Objects.isNull(login) || login == 0){
            model.addAttribute("msg", "请先登录");
            return "user/login";
        }
        String account = (String) session.getAttribute("user");
        String name = userService.getName(account);
        List<Room> rooms = roomService.getRooms(name);

        model.addAttribute("rooms", rooms);
        return "chat/room";
    }

    @GetMapping("/room/{id}")
    public String channel(HttpSession session, Model model, @PathVariable("id") Integer id){

        String account = (String) session.getAttribute("user");
        if(Objects.isNull(account)){
            model.addAttribute("msg", "请先登录");
            return "user/login";
        }
        Room room = roomService.getRoomByID(id);
        if(Objects.isNull(room)){
            model.addAttribute("msg", "房间不存在");
            String name = userService.getName(account);
            List<Room> rooms = roomService.getRooms(name);
            model.addAttribute("rooms", rooms);
            return "chat/room";
        }
        String name = userService.getName(account);
        if(!room.getUser1().equals(name) && !room.getUser2().equals(name)){
            model.addAttribute("msg", "没有访问权限");
            return "chat/room";
        }
        List<Message> messages = roomService.getMessage(id);

        // 房间信息
        model.addAttribute("room", room);
        // 消息记录返到房间，显示
        model.addAttribute("messages", messages);
        return "chat/channel";
    }

    @RequestMapping("pick")
    public String pick(HttpSession session, Model model){

        String account = (String) session.getAttribute("user");
        if(Objects.isNull(account)){
            model.addAttribute("msg", "请先登录");
            return "user/login";
        }
        String name = userService.getName(account);
        List<User> list = userService.getOnlineList(name);

        model.addAttribute("friends", list);
        return "chat/pick";
    }

    @RequestMapping("search")
    public String search(Model model, @RequestParam("account") String account){

        List<User> list = userService.searchUser(account);
        model.addAttribute("friends", list);
        return "chat/pick";
    }

    @GetMapping("/build/{name}")
    public String build(HttpSession session, Model model, @PathVariable("name") String user2) {
        String user1 = (String)session.getAttribute("user");
        Map<String, String> params = new HashMap<>();
        params.put("user1", user1);
        params.put("user2", user2);

//        System.out.println(user1 + " " + user2);
        roomService.add(params);

        List<Room> rooms = roomService.getRooms(user1);
        model.addAttribute("rooms", rooms);;
        return "chat/room";
    }

    @GetMapping("/delRoom/{id}")
    public String build(HttpSession session, Model model, @PathVariable("id") Integer id) {
        roomService.del(id);
        String user = (String)session.getAttribute("user");
        List<Room> rooms = roomService.getRooms(user);
        model.addAttribute("rooms", rooms);;
        return "chat/room";
    }

    @ResponseBody
    @RequestMapping("/insertMessage")
    public Map<String, Object> addMessage(@RequestParam("from") String from, @RequestParam("to") String to,
                             @RequestParam("room") Integer room, @RequestParam("content") String content){
        Map<String, Object> result = new HashMap<>();
        try{
            Message message = new Message(from, to, room, content);
            roomService.addMessage(message);
            result.put("flag", true);
        }catch (Exception e){
            e.printStackTrace();
            result.put("flag", false);
        }
        return result;
    }

}
