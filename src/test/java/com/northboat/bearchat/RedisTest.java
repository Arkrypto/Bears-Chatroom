package com.northboat.bearchat;


import com.northboat.bearchat.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void redisTest(){
        redisUtil.set("Northboat", 1);
        System.out.println(redisUtil.get("Northboat"));
        redisUtil.del("Northboat");
    }
}
