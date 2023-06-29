package com.northboat.bearchat.service.impl;


import com.northboat.bearchat.mapper.MessageMapper;
import com.northboat.bearchat.mapper.RoomMapper;
import com.northboat.bearchat.pojo.Message;
import com.northboat.bearchat.pojo.Room;
import com.northboat.bearchat.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
public class RoomServiceImpl implements RoomService {

    private RoomMapper roomMapper;
    @Autowired
    public void setRoomMapper(RoomMapper roomMapper){
        this.roomMapper = roomMapper;
    }

    private MessageMapper messageMapper;
    @Autowired
    public void setMessageMapper(MessageMapper messageMapper){
        this.messageMapper = messageMapper;
    }

    @Override
    public List<Room> getRooms(String name){
        List<Room> list = roomMapper.getRoomByUser1(name);
        List<Room> list1 = roomMapper.getRoomByUser2(name);
        list.addAll(list1);
        return list;
    }

    @Override
    public Room getRoomByID(int id){
        return roomMapper.getRoomByID(id);
    }

    @Override
    public void add(Map<String, String> params){
        roomMapper.addRoom(params);
    }

    @Override
    public void del(int id){
        roomMapper.delRoom(id);
    }

    @Override
    public void addMessage(Message msg){
        messageMapper.add(msg);
    }

    @Override
    public List<Message> getMessage(int room){
        return messageMapper.get(room);
    }

}
