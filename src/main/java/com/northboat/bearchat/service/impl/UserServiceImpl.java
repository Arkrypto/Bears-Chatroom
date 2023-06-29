package com.northboat.bearchat.service.impl;

import com.northboat.bearchat.mapper.UserMapper;
import com.northboat.bearchat.pojo.User;
import com.northboat.bearchat.service.UserService;
import com.northboat.bearchat.utils.MailUtil;
import com.northboat.bearchat.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Service
public class UserServiceImpl implements UserService {


    private UserMapper userMapper;
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    private RedisUtil redisUtil;
    @Autowired
    public void setRedisUtil(RedisUtil redisUtil){
        this.redisUtil = redisUtil;
    }

    private MailUtil mailUtil;
    @Autowired
    public void setMailUtil(MailUtil mailUtil){
        this.mailUtil = mailUtil;
    }


    private boolean containAt(String str){
        for(char c: str.toCharArray()){
            if(c == '@'){
                return true;
            }
        }
        return false;
    }

    public int sign(String account, String password){
        User user = userMapper.queryByEmail(account);
        if(Objects.isNull(user)){
            user = userMapper.queryByName(account);
        }
        if(Objects.isNull(user)){
            return 0;
        }
        if(user.getPassword().equals(password)){
            userMapper.online(user.getName());
            return 1;
        }
        return -1;
    }
    @Override
    public List<User> getOnlineList(String name){
        return userMapper.queryAllOnline();
    }


    @Override
    public int send(String account){
        // 把用户查出来，通过@判断传入的是昵称还是邮箱
        User user = containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        if(Objects.isNull(user)){
            String code;
            try{
                code = mailUtil.send(account, "");
            }catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            // 存验证码用邮箱存
            redisUtil.set(account, code, 600);
            return 2;
        }

        String code;
        try{
            code = mailUtil.send(user.getEmail(), user.getName());
        }catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        // 存验证码用邮箱存
        redisUtil.set(user.getEmail(), code, 600);
        return 1;
    }

    // 存入在线列表的就是用户网页 session 里的 user，前后端统一
    @Override
    public boolean verily(String account, String code){
        User user = containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        if(Objects.isNull(user)){
            return false;
        }
        String c = (String) redisUtil.get(user.getEmail());
        if(code.equals(c)){
            redisUtil.del(user.getEmail());
            return true;
        }
        return false;
    }

    // 存入在线列表的就是用户网页 session 里的 user，前后端统一
    @Override
    public int register(String email, String code, String name, String password) {
        String c = (String) redisUtil.get(email);
        if(Objects.isNull(c)){
            return 2;
        }
        if(!c.equals(code)){
            return 2;
        }
        if(!nameValid(name)){
            return 3;
        }
        redisUtil.del(email);
        User user = new User(email, name, password);
        userMapper.add(user);
        return 1;
    }

    @Override
    public boolean nameValid(String name){
        for(char c: name.toCharArray()){
            if(c == '@'){
                return false;
            }
        }
        for(User user: userMapper.queryAll()){
            if(user.getName().equals(name)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean logout(String account){
//        System.out.println(account);
        User user = userMapper.queryByEmail(account);
        if(Objects.isNull(user)){
            user = userMapper.queryByName(account);
        }
        if(Objects.isNull(user)){
//            System.out.println("nmsl");
            return false;
        }
        userMapper.logout(user.getName());
        return true;
    }

    @Override
    public String getName(String account){
        if(containAt(account)){
            return userMapper.queryByEmail(account).getName();
        }
        return account;
    }

    @Override
    public List<User> searchUser(String account){
        if(containAt(account)){
            return userMapper.searchUserByEmail(account);
        }
        return userMapper.searchUserByName(account);
    }
}
