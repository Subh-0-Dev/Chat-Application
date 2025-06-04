package com.subh.DemoChat.Controller;

import com.subh.DemoChat.DTOs.MessageResponse;
import com.subh.DemoChat.DTOs.RoomRequest;
import com.subh.DemoChat.Entity.Message;
import com.subh.DemoChat.Entity.Room;
import com.subh.DemoChat.Entity.UserEntity;
import com.subh.DemoChat.Repository.RoomRepository;
import com.subh.DemoChat.Repository.UserRepository;
import com.subh.DemoChat.Service.RoomService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    //creating room
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest room){
        Room addroom = roomService.addroom(room);
        if(addroom==null){
            return ResponseEntity.badRequest().body("Room Already Existed");

        }
        return ResponseEntity.status(HttpStatus.CREATED).body(addroom);
    }

    //joining room
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestBody RoomRequest roomRequest, Principal user) {
        Optional<UserEntity> byEmail = userRepository.findByEmail(user.getName());
        Room room = roomService.verify(roomRequest,byEmail.get());

        if (room == null) {
            return ResponseEntity.badRequest().body("Room not Found or Incorrect Password");
        }

        if (byEmail.isPresent()) {
            UserEntity currentUser = byEmail.get();

            if (currentUser.getJoinedRooms() == null) {
                currentUser.setJoinedRooms(new ArrayList<>());
            }

            if (!currentUser.getJoinedRooms().contains(room)) {
                currentUser.getJoinedRooms().add(room);
                userRepository.save(currentUser);
            }
        }

        return ResponseEntity.ok(room);
    }


    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable String roomId,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size",defaultValue = "20", required = false) int size
    ){
        Room room = roomRepository.findByRoomId(roomId);
        if(room==null){
            return ResponseEntity.badRequest().build();
        }
        List<Message> messages=room.getMessages();
        List<MessageResponse> messageList= new ArrayList<>();
        for(Message message : messages){
            MessageResponse response=new MessageResponse();
            response.setId(message.getId());
            response.setContent(message.getContent());
            response.setUsername(message.getUserEntity().getUsername());
            response.setRoomname(message.getRoom().getRoomId());
            response.setTimeStamp(message.getTimeStamp());

            messageList.add(response);
        }

        int start=Math.max(0,messages.size() - (page+1) * size);
        int end=Math.min(messages.size(),start+size);

        messages.subList(start,end);

        return ResponseEntity.ok(messageList);
    }


        @PostMapping("/{roomId}/leave")
        public ResponseEntity<String> leaveRoom(
                @PathVariable String roomId,
                @RequestBody Map<String, String> request) {

            String username = request.get("username");

            // Optional: Log who left
            System.out.println("User " + username + " left room " + roomId);

            // Optional: Broadcast leave message to other users
            // messagingTemplate.convertAndSend("/topic/room/" + roomId,
            //     createSystemMessage(username + " left the room"));

            return ResponseEntity.ok("Left room successfully");
        }

    @GetMapping("/joined")
    public ResponseEntity<?> getJoinedRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<UserEntity> userOpt = userRepository.findByEmail(username);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            return ResponseEntity.ok(user.getJoinedRooms());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }


}
