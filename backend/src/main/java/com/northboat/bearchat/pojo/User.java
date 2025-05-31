package com.northboat.bearchat.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
public class User {

    private String name;

    private String email;

    private String password;

}
