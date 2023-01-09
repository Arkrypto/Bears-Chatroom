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

    @Override
    public List<User> getUserList(){
        return userMapper.queryAll();
    }
    @Override
    public int send(String account){
        // 把用户查出来，通过@判断传入的是昵称还是邮箱
        User user = containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);

        String email = Objects.isNull(user) ? account : user.getEmail();
        String name = Objects.isNull(user) ? "" : user.getName();
        int flag = Objects.isNull(user) ? 2 : 1;

        String code;
        try{
            code = mailUtil.send(email, name);
        }catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        // 存验证码用邮箱存
        redisUtil.set(email, code, 600);
        return flag;
    }

    // 存入在线列表的就是用户网页 session 里的 user，前后端统一
    @Override
    public boolean verily(String account, String code){
        User user = containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        String c = (String) redisUtil.get(user.getEmail());
        if(c.equals(code)){
            String name = user.getName();
            // 加入在线队列
            redisUtil.sadd("online", name);
            redisUtil.del(user.getEmail());
            return true;
        }
        return false;
    }

    // 存入在线列表的就是用户网页 session 里的 user，前后端统一
    @Override
    public int register(String email, String code, String name) {
        String c = (String) redisUtil.get(email);
        if(!c.equals(code)){
            return 2;
        }
        for(User user: userMapper.queryAll()){
            if(user.getName().equals(name)){
                return 3;
            }
        }
        redisUtil.del(email);
        return 1;
    }

    public boolean addToDB(User user){
        userMapper.addEmail(user);
        userMapper.addName(user);
        // 将邮箱作为用户名存入在线列表
        redisUtil.sadd("online", user.getName());
        return true;
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

    // 选取私人房间号返回
    @Override
    public String pick(String account){
        // 房间号统一用昵称存
        User user = containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        String self = user.getName();
        String room = (String) redisUtil.get(self);
        // 如果已有房间，直接返回
        if(!Objects.isNull(room)){
            return room;
        }
        // 否则找一个在线用户，组建房间返回房间号
        for(Object onlineUser: redisUtil.sget("online")){
            // 获取在线用户的名字
            String friend = (String) onlineUser;
            if(friend.equals(self)){
                continue;
            }

            // 如果当前用户在 redis 里存的房间为空，说明暂未配对
            room = (String) redisUtil.get(friend);

            // 如果未配对，让这个用户和传进来的用户组建房间并返回房号
            if(Objects.isNull(room)){
                String tag = mailUtil.generateCode();
                // 设置房间有效时间为1天
                // 双向绑定
                redisUtil.set(self, tag, 86400);
                redisUtil.set(friend, tag, 86400);
                // 把名字存到房间号
                redisUtil.rpush(tag, self);
                redisUtil.rpush(tag, friend);
                redisUtil.expire(tag, 86400);
                return tag;
            }
        }
        // 若没找到在线空闲用户，返回"null"
        return "null";
    }

    public List getRoom(String room){
        return redisUtil.lget(room);
    }

    // 用户主动关闭房间
    public String close(String user){
        return null;
    }

    @Override
    public boolean logout(String account){
        User user = containAt(account) ? userMapper.queryByEmail(account) : userMapper.queryByName(account);
        String name = user.getName();
        try{
            redisUtil.srem("online", name);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
