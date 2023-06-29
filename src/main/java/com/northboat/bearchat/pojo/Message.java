package com.northboat.bearchat.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
public class Message {
    private int id;
    private String from;
    private String to;
    private int received;
    private int room;
    private String content;
    public Message(String from, String to, int room, String content){
        this.from = from;
        this.to = to;
        this.room = room;
        this.content = content;
    }
}
