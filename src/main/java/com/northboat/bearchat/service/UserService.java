package com.northboat.bearchat.service;

import com.northboat.bearchat.pojo.User;

import java.util.List;

public interface UserService {

    public int register(String email, String code, String name);
    public List<User> getUserList();
    public int send(String email);
    public boolean verily(String account, String code);
    public boolean nameValid(String name);
    public List<String> getRoom(String room);
    public String pick(String curUser);
    public boolean addToDB(User user);
    public boolean logout(String user);

}
