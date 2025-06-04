package com.subh.DemoChat.Service;

import com.subh.DemoChat.DTOs.RoomRequest;
import com.subh.DemoChat.Entity.Room;
import com.subh.DemoChat.Entity.UserEntity;
import com.subh.DemoChat.Repository.RoomRepository;
import com.subh.DemoChat.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    public Room verify(RoomRequest room, UserEntity user) {
        Room byRoomId = roomRepository.findByRoomId(room.getRoomId());

        if (byRoomId == null) {
            return null;
        }


        if (user.getJoinedRooms().contains(byRoomId)) {
            return byRoomId;
        }

        // 🔒 Otherwise, check password
        if (byRoomId.getRoomPassword() != null && !byRoomId.getRoomPassword().equals(room.getRoomPassword())) {
            return null;
        }

        return byRoomId;
    }


    public Room addroom(RoomRequest room) {
        Room byRoomId = roomRepository.findByRoomId(room.getRoomId());
        if(byRoomId != null){
            return null;
        }
        Room newRoom=new Room();
        newRoom.setRoomId(room.getRoomId());
        newRoom.setRoomPassword(room.getRoomPassword());
        return roomRepository.save(newRoom);
    }
}
