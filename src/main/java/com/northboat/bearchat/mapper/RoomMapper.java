package com.northboat.bearchat.mapper;

import com.northboat.bearchat.pojo.Room;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface RoomMapper {
    List<Room> getRoomByUser1(String user1);

    List<Room> getRoomByUser2(String user2);

    Room getRoomByID(int id);

    void addRoom(Map<String, String> params);

    void delRoom(int id);
}
