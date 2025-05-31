package com.northboat.bearchat.service;

import com.northboat.bearchat.pojo.User;

import java.util.List;

public interface UserService {

    public int register(String email, String code, String name, String password);
    public List<User> getOnlineList(String name);
    public int send(String email);
    public int sign(String account, String password);
    public boolean verily(String account, String code);
    public boolean nameValid(String name);
    public boolean logout(String user);
    public String getName(String account);
    public List<User> searchUser(String account);

}
