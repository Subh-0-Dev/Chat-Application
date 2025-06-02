package com.subh.DemoChat.Service;

import com.subh.DemoChat.DTOs.MessageRequest;
import com.subh.DemoChat.Entity.Message;
import com.subh.DemoChat.Entity.Room;
import com.subh.DemoChat.Entity.UserEntity;
import com.subh.DemoChat.Repository.RoomRepository;
import com.subh.DemoChat.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;


    public Message sendMessages(MessageRequest request, String name) {
        Room room = roomRepository.findByRoomId(request.getRoomId());
        Optional<UserEntity> currentUser=userRepository.findByEmail(name);
        Message message=new Message();
        message.setContent(request.getContent());
        message.setTimeStamp(LocalDateTime.now());

        if(room!=null && currentUser.isPresent()){
            message.setUserEntity(currentUser.get());
            message.setRoom(room);
            room.getMessages().add(message);
            roomRepository.save(room);
        }else {
            throw new RuntimeException("Room not Found");
        }
        return message;
    }
}
