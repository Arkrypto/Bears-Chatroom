package com.northboat.bearchat.mapper;

import com.northboat.bearchat.pojo.Message;
import com.northboat.bearchat.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MessageMapper {
    public void add(Message message);

    public List<Message> get(int room);
}
