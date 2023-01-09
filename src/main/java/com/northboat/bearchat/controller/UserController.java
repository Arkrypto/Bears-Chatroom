package com.northboat.bearchat.controller;

import com.northboat.bearchat.pojo.User;
import com.northboat.bearchat.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class UserController {

    private UserServiceImpl userService;
    @Autowired
    public void setUserService(UserServiceImpl userService){
        this.userService = userService;
    }

    @RequestMapping("/login")
    public String login(HttpSession session, Model model){
        Integer login = (Integer) session.getAttribute("login");
        if(!Objects.isNull(login)){
            String user = (String) session.getAttribute("user");
            model.addAttribute("login", 1);
            model.addAttribute("user", user);
        }
        return "user/login";
    }

    // 发送邮件
    @RequestMapping("/send")
    public String send(Model model, HttpSession session, @RequestParam("account") String account){
        int status = userService.send(account);
        System.out.println(status);
        session.setAttribute("user", account);
        if(status == 1){
            return "user/verify";
        } else if(status == 2){
            return "user/register";
        }
        model.addAttribute("msg", "验证码发送失败");
        return "user/login";
    }

    // 已注册，登录验证
    @RequestMapping("/verify")
    public String verify(Model model, HttpSession session, @RequestParam("code") String code){
        String account = (String) session.getAttribute("user");
        if(Objects.isNull(account)){
            model.addAttribute("msg", "请先获取验证码");
            return "user/login";
        }
        if(userService.verily(account, code)){
            System.out.println("登录成功");
            // 登录成功
            session.setAttribute("login", 1);
            model.addAttribute("login", 1);
            model.addAttribute("user", account);
            return "user/login";
        }
        model.addAttribute("msg", "验证码错误");
        return "user/verify";
    }

    // 已注册，登录验证
    @RequestMapping("/register")
    public String register(Model model, HttpSession session, @RequestParam("code") String code, @RequestParam("name") String name){
        String email = (String) session.getAttribute("user");
        if(Objects.isNull(email)){
            model.addAttribute("msg", "请先获取验证码");
            return "user/login";
        }
        int flag = userService.register(email, code, name);
        if(flag == 2){
            model.addAttribute("msg", "验证码错误");
            return "user/register";
        } else if(flag == 3){
            model.addAttribute("msg", "昵称已被使用或含有字符@");
            return "user/register";
        }
        User user = new User(name, email);
        userService.addToDB(user);
        session.setAttribute("login", 1);
        model.addAttribute("login", 1);
        model.addAttribute("user", name);
        return "user/login";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session, Model model){
        String user = (String) session.getAttribute("user");
        if(!userService.logout(user)){
            model.addAttribute("msg", "退出登录失败");
            return "user/login";
        }
        session.removeAttribute("user");
        session.removeAttribute("login");
        return "user/login";
    }
}
