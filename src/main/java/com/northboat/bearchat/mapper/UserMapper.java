package com.northboat.bearchat.mapper;

import com.northboat.bearchat.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserMapper {

    public List<User> queryAll();

    public void addEmail(User user);

    public void addName(User user);

    public User queryByName(String name);

    public User queryByEmail(String email);
}
