package com.subh.DemoChat.Controller;

import com.subh.DemoChat.Entity.Message;
import com.subh.DemoChat.Entity.Room;
import com.subh.DemoChat.Repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    //creating room
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody String roomId){
        if(roomRepository.findByRoomId(roomId) != null){
            return ResponseEntity.badRequest().body("Room Already Existed");
        }
        Room room=new Room();
        room.setRoomId(roomId);

        Room newRoom = roomRepository.save(room);

        return ResponseEntity.status(HttpStatus.CREATED).body(newRoom);
    }

    //joining room
    @GetMapping("/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId){

        Room room=roomRepository.findByRoomId(roomId);
        if(room==null){
            return ResponseEntity.badRequest().body("Room not Found");
        }
        return ResponseEntity.ok(room);
    }


    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String roomId,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size",defaultValue = "20", required = false) int size
    ){
        Room room = roomRepository.findByRoomId(roomId);
        if(room==null){
            return ResponseEntity.badRequest().build();
        }
        List<Message> messages=room.getMessages();

        int start=Math.max(0,messages.size() - (page+1) * size);
        int end=Math.min(messages.size(),start+size);

        messages.subList(start,end);

        return ResponseEntity.ok(messages);
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

}
