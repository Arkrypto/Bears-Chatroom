package com.northboat.bearchat.mapper;

import com.northboat.bearchat.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserMapper {

    public List<User> queryAll();
    public List<User> queryAllOnline();
    public void add(User user);
    public int online(String name);
    public User queryByName(String name);
    public User queryByEmail(String email);
    public void logout(String name);
    public List<User> searchUserByName(String name);
    public List<User> searchUserByEmail(String email);
}
