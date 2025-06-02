package com.subh.DemoChat.Controller;

import com.subh.DemoChat.DTOs.MessageRequest;
import com.subh.DemoChat.DTOs.MessageResponse;
import com.subh.DemoChat.Entity.Message;
import com.subh.DemoChat.Entity.Room;
import com.subh.DemoChat.Entity.UserEntity;
import com.subh.DemoChat.Repository.RoomRepository;
import com.subh.DemoChat.Repository.UserRepository;
import com.subh.DemoChat.Service.MessageService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@CrossOrigin("*")
public class ChatController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private MessageService messageService;
    @MessageMapping("/sendMessage/{roomId}") // /app/sendMessage
//    @SendTo("/topic/room/{roomId}")
    public MessageResponse sendMessage(
            @DestinationVariable String roomId,
            @RequestBody MessageRequest request,
            Principal user
    ){

        Message message = messageService.sendMessages(request, user.getName());
        MessageResponse response=new MessageResponse();
        if(message != null){
            response.setId(message.getId());
            response.setContent(message.getContent());
            response.setTimeStamp(message.getTimeStamp());
            response.setUsername(message.getUserEntity().getUsername());
            response.setRoomname(message.getRoom().getRoomId());
            messagingTemplate.convertAndSend("/topic/room/" + roomId, response);
        }
        return response;
    }
}
