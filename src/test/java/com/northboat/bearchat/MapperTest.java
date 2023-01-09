package com.northboat.bearchat;

import com.northboat.bearchat.mapper.UserMapper;
import com.northboat.bearchat.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
public class MapperTest {
    @Autowired
    UserMapper userMapper;
    @Test
    public void UserMapperTest(){
        List<User> l = userMapper.queryAll();
        for(User u: l){
            System.out.println(u.getName() + " " + u.getEmail());
        }
        User user = new User("Northboat", "northboat@163.com");
        //userMapper.addName(user);
        userMapper.addEmail(user);

    }
}
