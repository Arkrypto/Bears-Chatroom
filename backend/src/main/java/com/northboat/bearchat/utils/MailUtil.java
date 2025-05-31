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

    public String send(String to, String name) throws org.springframework.mail.MailSendException{
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
