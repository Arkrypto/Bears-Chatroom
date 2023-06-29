package com.northboat.bearchat.service;

import com.northboat.bearchat.pojo.Message;
import com.northboat.bearchat.pojo.Room;
import com.northboat.bearchat.pojo.User;

import java.util.List;
import java.util.Map;

public interface RoomService {

    void add(Map<String, String> params);

    List<Room> getRooms(String name);

    Room getRoomByID(int id);

    void del(int id);

    void addMessage(Message msg);

    List<Message> getMessage(int room);
}
