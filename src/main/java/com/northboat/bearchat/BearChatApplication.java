package com.northboat.bearchat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@MapperScan("com.northboat.bearchat.mapper")
@EnableWebSocket
public class BearChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(BearChatApplication.class, args);
	}

}
