## 前期准备

### SpringBoot 文件配置

下载 jdk17

```bash
yay -S jdk17-openjdk
```

创建 springboot3.0 工程，导入依赖 pom.xml

- `springboot-web, springboot-starter`
- `redis`
- `thymeleaf`
- `mysql-driver, mybatis`
- `springboot-test, junit`
- `lombok`
- `springboot-mail`
- `websocket`

pom.xml

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>3.0.1</version>
    </dependency>

    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>3.5.11</version>
    </dependency>

    <!--lombok-->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>


    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!--junit-->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
    </dependency>
    <!--mail-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <!--WebSocket-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
</dependencies>
```

配置文件，application.yaml

- `mysql`
- `mybatis`（xml 路径配置），同时要在主函数上加注解`@ScanMapper("com.northboat.bearchat.mapper")`
- `redis`
- `mail`

```yaml
spring:
  application:
    name: Bear-Chat
  datasource:
    username: root
    password: "011026"
    url: jdbc:mysql://localhost:3306/bearchat?useUnicode=true&characterEncoding=utf-8
    driver-class-name: com.mysql.cj.jdbc.Driver
  thymeleaf:
    cache: false
  data:
    redis:
      host: localhost
      port: 6379
      password: "011026"
  mail:
    host: smtp.qq.com
    username: "northboat@qq.com"
    password: "oxftgstrzznrbddc"
    #oxftgstrzznrbddc

#整合mybatis
mybatis:
  type-aliases-package: com.northboat.bearchat.pojo
  # classpath 指 resources 目录
  mapper-locations: classpath:/mapper/*.xml

server:
  port: 8080
```

安放前端文件：将 index.html 放在 template 文件夹下，样式文件放在 static 文件夹下，html 中路径`/img/1.png`即表示`static/img/1.png`，以 static 为根目录

### Mail 工具类

MailUtil.java

```java
package com.northboat.bearchat.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailUtil extends Thread {

    @Autowired
    private JavaMailSender javaMailSender;

    //邮件信息
    private static final String from = "northboat@qq.com";


    public String getFrom(){
        return from;
    }

    //生成6位验证码，包含数字、小写字母、大写字母
    public String generateCode(){
        char[] code = new char[6];
        for(int i = 0; i < 6; i++){
            //floor向下取整，random生成数[0,1)
            int flag = (int)Math.floor(1+Math.random()*3);
            switch (flag) {
                case 1 -> code[i] = (char) Math.floor(48 + Math.random() * 10); //48-57数字
                case 2 -> code[i] = (char) Math.floor(97 + Math.random() * 26);
                case 3 -> code[i] = (char) Math.floor(65 + Math.random() * 26);
            }
        }
        return new String(code);
    }

    public String send(String to, String name){
        String code = generateCode();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);

        message.setTo(to);
        message.setSubject("Hello~" + name);
        message.setText("这是您的验证码：" + code);
        javaMailSender.send(message);

        return code;
    }
}
```

### Redis 配置及工具类

RedisConfig.java：重写 RedisTemplate

```java
package com.northboat.bearchat.config;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.UnknownHostException;

@Configuration
public class RedisConfig {

    //一个固定的模板，在企业中可以直接使用，几乎包含了所有场景
    //编写我们自己的RedisTemplate
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> myRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        //Json序列化配置
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        //String序列化配置
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key和Hash的key使用String序列化
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value和Hash的value使用Jackson序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

RedisUtil.java：Redis 工具类，实现各种 Redis 操作

```java
package com.northboat.bearchat.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;


// 待完善
@Component
@SuppressWarnings("all")
public class RedisUtil {

    private RedisTemplate myRedisTemplate;
    @Autowired
    public void setMyRedisTemplate(RedisTemplate myRedisTemplate){
        this.myRedisTemplate = myRedisTemplate;
    }


    //设置有效时间，单位秒
    public boolean expire(String key, long time){
        try{
            if(time > 0){
                myRedisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //获取剩余有效时间
    public long getExpire(String key){
        return myRedisTemplate.getExpire(key);
    }

    //判断键是否存在
    public boolean hasKey(String key){
        try{
            return myRedisTemplate.hasKey(key);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //批量删除键
    public void del(String... key){
        if(key != null && key.length > 0){
            if(key.length == 1){
                myRedisTemplate.delete(key[0]);
            } else {
                myRedisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    //获取普通值
    public Object get(String key){
        return key == null ? null : myRedisTemplate.opsForValue().get(key);
    }

    //放入普通值
    public boolean set(String key, Object val){
        try{
            myRedisTemplate.opsForValue().set(key, val);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //放入普通缓存并设置时间
    public boolean set(String key, Object val, long time){
        try{
            if(time > 0){
                myRedisTemplate.opsForValue().set(key, val, time, TimeUnit.SECONDS);
            } else { // 若时间小于零直接调用普通设置的方法放入
                this.set(key, val);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    //值增
    public long incr(String key, long delta){
        if(delta < 0){
            throw new RuntimeException("递增因子必须大于零");
        }
        return myRedisTemplate.opsForValue().increment(key, delta);
    }

    //============Map=============

    // 获取key表中itme对应的值
    public Object hget(String key, String item){
        return myRedisTemplate.opsForHash().get(key, item);
    }

    // 获取整个Hash表
    public Map hmget(String key){
        return myRedisTemplate.opsForHash().entries(key);
    }

    // 简单设置一个Hash
    public boolean hmset(String key, Map<String, Object> map){
        try{
            myRedisTemplate.opsForHash().putAll(key, map);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // 设置一个Hash，并设置生效时间，调用上面的设置key生效时间的方法
    public boolean hmset(String key, Map<String, Object> map, long time){
        try{
            myRedisTemplate.opsForHash().putAll(key, map);
            if(time > 0){
                this.expire(key, time);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }


    // 像一张Hash表中添加键值，若表不存在将创建
    public boolean hset(String key, String item, Object val){
        try{
            myRedisTemplate.opsForHash().put(key, item, val);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
```

### 前期排错

SpringBoot 启动警告：OpenJDK 64-Bit Server VM warning: Options -Xverify:none and -noverify were deprecated in JDK 13...

- 点击右上角 springboot 配置，`edit configuration - modify option - disable launch optimization`

连接不到 mysql

- application.yaml 中把 mysql 的驱动 driver 由 com.mysql.jdbc.Driver 换为 com.mysql.cj.jdbc.Driver
- 可以通过 idea 右侧的 database 尝试连接 mysql

JavaMailSender 在注入时会爆红，说注入失败，但是实际上能跑，我怀疑是 idea 没读到 application,yaml 中 spring.mail 的配置

SpringBoot3.x 必须要求 JDK 版本 >= 17，否则会有很多父级依赖报错

## 帐号登录

只有一个表

```sql
CREATE TABLE `user` (
  `name` varchar(21) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`name`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
```

### Mapper

记得在启动类上加一个扫描 mapper 的注解

```java
@SpringBootApplication
@MapperScan("com.northboat.bearchat.mapper")
public class BearChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(BearChatApplication.class, args);
	}

}
```

UserMapper.java

```java
package com.northboat.bearchat.mapper;

import com.northboat.bearchat.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface UserMapper {

    public List<User> queryAll();
    public void add(User user);
    public User queryByName(String name);
    public User queryByEmail(String email);
}
```

UserMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.northboat.bearchat.mapper.UserMapper">
    <select id="queryAll" resultType="com.northboat.bearchat.pojo.User">
        select * from user
    </select>

    <insert id="add" parameterType="com.northboat.bearchat.pojo.User">
        insert into user values (#{name},#{email})
    </insert>
    
    <select id="queryByName" resultType="com.northboat.bearchat.pojo.User">
        select * from user where name = #{name}
    </select>

    <select id="queryByEmail" resultType="com.northboat.bearchat.pojo.User">
        select * from user where email = #{email}
    </select>

</mapper>
```

### Service

整合 Mapper、MailUtil、RedisUtil，实现上层功能

UserService.java

- 登录：从前端用户输入的帐号，通过是否含有@判断是邮箱还是昵称，从数据库中拿取 user（若为空，则以帐号为邮箱发送验证码，这里要有异常捕获，发现异常返回 0，告知前端），生成 6 位验证码发送邮件，同时以`<邮箱, 验证码>`的形式存入 Redis（设置有效时长10分钟）
- 验证：登陆时判空可以得知是否注册过，若注册过且邮件发送成功（send 返回 1），验证时从 session 中获取帐号，同理拿取 user，把邮箱和验证码传入 redis 判断是否正确，正确后记得删除 redis 键值
- 注册：若未注册过且邮件发送成功，跳转注册界面，要求输入昵称和验证码，需要验证码判断以及昵称判重，通过后再写入 mysql 数据库
- 退出登录：登录成功或注册成功后将把昵称加入 redis 的一个叫做 online 的 set 中表示在线，退出即在 online 中删除当前昵称即可，同时删除 session

```java
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
```

UserServiceImpl.java

- 登录验证码在 redis 里以`<邮箱，验证码>`的形式储存
- 房间号在 redis 里以`<昵称，房间号>`的形式储存，同时以`<房间号，List<用户昵称>>`的方式记录房间中用户

```java
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
        userMapper.add(user);
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
```

### Controller

Thymeleaf 用单页面根本实现不好，我放弃了，转而用`th:herf`的形式调用端口，通过`model`将数据传回一个个 html 页面，用`session`实现更远的消息传递

UserController.java

```java
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
```

### Thymeleaf

在后端 java 将 model.addAttribute 数据后，返回前端页面，如 user/login，在 html 中以键值对的形式调用 model 中的数据

示例

- `th:if="${login} eq 1"`：判断参数 login 是否为 1，为真则显示这段 html 标签
- `th:text="${user}"`：将数据 user 以文本形式插入标签
- `th:href="@{/logout}"`：调用后端接口`/logout`

```html
<section th:if="${login} eq 1">
    <h3>你好 <strong th:text="${user}"></strong></h3><br>
    <ul class="actions">
        <li><a th:href="@{/logout}">退出登录</a></li>
    </ul>
</section>
```

循环输出

- `th:each="user:${list}"`：从 collection 中顺序取出元素，赋予 user，相当于`for(int i: list)`
- 在作用域里对 user 进行操作

```html
<ul class="alt" th:each="user:${list}">
    <li th:text="${user.getName()}"></li>
</ul>
```

## 聊天实现

### 注入 Bean

配置单例，注入 bean

WebsocketConfig.java

```java
package com.northboat.bearchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    /**
     * WebSocket 服务器节点
     *
     * 如果使用独立的servlet容器，而不是直接使用springboot的内置容器，就不要注入ServerEndpointExporter，因为它将由容器自己提供和管理
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

### Socket 服务器实现

WebSocket 服务器

WebSocketServer.java

```java
package com.northboat.bearchat.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@Service
@ServerEndpoint("/chat/{sid}")
public class WebSocketServer {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static final CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //接收sid
    private String sid = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        this.sid = sid;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        try {
            sendMessage("Connection Test Message");
            log.info("有新窗口开始监听:" + sid + ", 当前总共在线人数为:" + getOnlineCount());
        } catch (IOException e) {
            log.error("websocket IO Exception");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        //断开连接情况下，更新主板占用情况为释放
        log.info("释放的sid为：" + sid);
        //这里写你 释放的时候，要处理的业务
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     * @ Param message 客户端发送过来的消息
     */
    // 全双工通信，服务器接收到客户端的消息后进行处理，可以是分发，可以是其他
    // 这里是将信息发送给 sid 相同的所有连接
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来自窗口 " + sid + " 的信息:" + message);
        //群发消息
        for (WebSocketServer item : webSocketSet) {
            if(item.sid.equals(sid)){
                try {
                    item.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @ Param session
     * @ Param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message, @PathParam("sid") String sid) throws IOException {
        log.info("推送消息到窗口" + sid + "，推送内容:" + message);

        for (WebSocketServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if (sid == null) {
                    item.sendMessage(message);
                } else if (item.sid.equals(sid)) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

    public static CopyOnWriteArraySet<WebSocketServer> getWebSocketSet() {
        return webSocketSet;
    }
}
```

最后别忘了在启动类上加入注解，允许 WebSocket 服务器运行

```java
@EnableWebSocket
```

### 公共聊天室实现

sid 为 park，ChatController.java

```java
private UserService userService;
@Autowired
public void setUserService(UserService userService){
    this.userService = userService;
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
    return "chat/park";
}

```

公共聊天室前端，就是通过 url 建立一个 websocket，后端发现请求后立马将其加入 server-set 进行管理

全双工通信

- 客户端可以主动向服务器发送信息，即前端调用 WebSocket.send() 函数
- 后端编写的 WebSocketServer 将处理接收到的客户端信息，通过 set 中存储的一个个 session，即和一个个客户端的会话，将收到的消息根据 sid 群发出去，客户端由于 session 和 websocket 的工作机制，将时刻监听这个消息，同时在前端作出相应反应

前端部分代码

```html
<h2 class="major">共有<strong th:text="${count}"></strong>人在公共聊天室</h2>

<p id="user" th:text="${name}" style="display: none"></p>
<p id="login" th:text="${login}" style="display: none"></p>
<p id="room" th:text="${room}" style="display: none"></p>
<script>
    let user = document.getElementById("user").innerText;
    let login = document.getElementById("login").innerText;
    console.log(name);

    // 连接 websocket 服务器
    let websocket = null;
    let room  = document.getElementById("room").innerText;
    //判断当前浏览器是否支持WebSocket
    if(room === "null"){
        throw SyntaxError();
    } else if('WebSocket' in window) {
        //改成你的地址http
        websocket = new WebSocket("ws://fx35xt.natappfree.cc/chat/"+room);
    } else {
        alert('当前浏览器 Not support websocket')
    }


    //连接发生错误的回调方法
    websocket.onerror = function() {
        setMessageInnerHTML("WebSocket 连接发生错误");
    };

    //连接成功建立的回调方法
    websocket.onopen = function() {
        setMessageInnerHTML("WebSocket 连接成功");
    }
    //let U01data, Uidata, Usdata;
    //接收到消息的回调方法
    websocket.onmessage = function(event) {
        console.log(event);
        setMessageInnerHTML(event.data);
        //setechart()
    }

    //连接关闭的回调方法
    websocket.onclose = function() {
        setMessageInnerHTML("WebSocket 连接关闭");
    }

    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function() {
        closeWebSocket();
    }

    //将消息显示在网页上
    function setMessageInnerHTML(innerHTML) {
        while(innerHTML.length > 32){
            document.getElementById('message').innerHTML += innerHTML.substring(0, 32) + '<br/>';
            innerHTML = innerHTML.substring(32);
        }
        document.getElementById('message').innerHTML += innerHTML + '<br/><br/>';
    }

    //关闭WebSocket连接
    function closeWebSocket() {
        websocket.close();
    }

    //发送消息
    function send() {
        let message = document.getElementById('text').value;
        websocket.send(message);
        document.getElementById("text").value = "";
        // websocket.send('{"msg":"' + message + '"}');
        // setMessageInnerHTML(message + "&#13;");
    }
</script>
```

### 私人聊天室实现

数据库表项

room

<img src="./assets/image-20230629092630003.png">

message

<img src="./assets/image-20230629092716402.png">

通过聊天室 id 创建 socket 信道，给予用户是否存储消息记录的选项，每次进入房间（id 对应的 socket 信道时，将历史记录返回到前端并显示）

通过 from/to 字段查找当前用户所拥有的房间号

## 测试

通过`natapp`进行内网穿透

官网：[natapp](https://natapp.cn/)

登录注册后可以选择购买免费隧道，配置本地端口，获取隧道的 authtoken

下载 natapp-linux，命令为，`xxxx`为我的隧道对应 token

```bash
./natapp -authtoken=xxxx
```

程序将把网址返回，注意前端代码中一些调用 url 的部分要换成相应网址，如

```js
websocket = new WebSocket("ws://localhost:8080/chat/"+room);
```

改成

```js
websocket = new WebSocket("ws://bp7fgk.natappfree.cc/chat/"+room);
```

