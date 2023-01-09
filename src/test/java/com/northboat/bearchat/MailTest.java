package com.northboat.bearchat;


import com.northboat.bearchat.utils.MailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


@SpringBootTest
public class MailTest {

    @Autowired
    private MailUtil mailUtil;

    @Test
    public void mailTest() {
        System.out.println(mailUtil.send("northboat@163.com", "Northboat"));
    }
}
