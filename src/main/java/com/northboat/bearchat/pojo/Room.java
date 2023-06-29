package com.northboat.bearchat.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
public class Room {
    private int id;
    String user1;
    String user2;
}
