package com.subh.DemoChat.Controller;

import com.subh.DemoChat.DTOs.SingleRequest;
import com.subh.DemoChat.DTOs.SingleResponse;
import com.subh.DemoChat.DTOs.profileResponseDto;
import com.subh.DemoChat.Entity.SingleMessage;
import com.subh.DemoChat.Entity.UserEntity;
import com.subh.DemoChat.Repository.SingleRepository;
import com.subh.DemoChat.Repository.UserRepository;
import com.subh.DemoChat.Service.SingleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@CrossOrigin("*")
public class SingleController {

    @Autowired
    private SingleService singleService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SingleRepository singleRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/onechat")
    public String chat() {
        return "onechat";
    }

    // WebSocket message handler - this is what was missing!
    @MessageMapping("/sendMessages/{receiverUsername}")
    public void sendMessageViaWebSocket(
            @DestinationVariable String receiverUsername,
            @Payload SingleRequest messageRequest,
            Authentication authentication) {

        try {
            String senderUsername = authentication.getName();
            System.out.println("=== WebSocket Message Debug ===");
            System.out.println("Sender: " + senderUsername);
            System.out.println("Receiver: " + receiverUsername);
            System.out.println("Content: " + messageRequest.getContent());

            // Save message to database
            SingleMessage savedMessage = singleService.sendMessage(senderUsername, receiverUsername, messageRequest.getContent());

            if (savedMessage != null) {
//                SingleResponse response=new SingleResponse();
//                response.setId(savedMessage.getMessageId());
//                response.setContent(savedMessage.getMessage());
//                response.setReceiverName(savedMessage.getReceiver().getEmail());
//                response.setSenderName(savedMessage.getSender().getEmail());
//                response.setTimeStamp(savedMessage.getTimestamp());
                // Send the complete message object to the receiver
                messagingTemplate.convertAndSendToUser(
                        savedMessage.getReceiver().getEmail(),
                        "/queue/messages",
                        savedMessage // Send the full message object instead of just a string
                );
                messagingTemplate.convertAndSendToUser(
                        savedMessage.getSender().getEmail(),
                        "/queue/messages",
                        savedMessage // Send the full message object instead of just a string
                );

                System.out.println("Message saved and sent successfully");
            } else {
                System.out.println("Failed to save message");
            }
        } catch (Exception e) {
            System.out.println("Error processing WebSocket message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Keep the REST endpoint for alternative usage
    @PostMapping("/send/{receiverUsername}")
    public ResponseEntity<String> sendMessage(
            @PathVariable String receiverUsername,
            @RequestBody SingleRequest content,
            Principal principal) {

        try {
            String senderUsername = principal.getName();
            SingleMessage message = singleService.sendMessage(senderUsername, receiverUsername, content.getContent());
            SingleResponse response=new SingleResponse();
            response.setId(message.getMessageId());
            response.setContent(message.getMessage());
            response.setReceiver(message.getReceiver());
            response.setSender(message.getSender());
            response.setTimeStamp(message.getTimestamp());
            if (message != null) {
                System.out.println(receiverUsername);
                // Notify the receiver via WebSocket
                messagingTemplate.convertAndSendToUser(
                        receiverUsername,
                        "/queue/messages",
                        response // Send the full message object
                );
                messagingTemplate.convertAndSendToUser(
                        senderUsername,
                        "/queue/messages",
                        response // Send the full message object
                );
                return ResponseEntity.ok("Message sent successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to send message");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/between/{user1}/{user2}")
    public ResponseEntity<List<SingleResponse>> getMessagesBetweenUsers(
            @PathVariable String user1,
            @PathVariable String user2,
            Authentication authentication) {


        String currentUser = authentication.getName();

        System.out.println("=== DEBUG INFO ===");
        System.out.println("Current authenticated user: '" + currentUser + "'");
        System.out.println("Requested user1: '" + user1 + "'");
        System.out.println("Requested user2: '" + user2 + "'");
        System.out.println("user1 equals currentUser: " + currentUser.equals(user1));
        System.out.println("user2 equals currentUser: " + currentUser.equals(user2));

        try {
            Optional<UserEntity> currentUserOpt = userRepository.findByEmail(currentUser);
            if (currentUserOpt.isEmpty() ||
                    (!currentUserOpt.get().getEmail().equals(user1) &&
                            !currentUserOpt.get().getEmail().equals(user2))) {

                System.out.println("ACCESS DENIED: User not part of conversation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }


            List<SingleResponse> messages = singleService.getMessagesBetweenUsers(user1, user2);
            System.out.println("Found " + messages.size() + " messages between users");
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            System.out.println("Error fetching messages: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<profileResponseDto>> getChatContacts(Authentication authentication) {
        String currentUsername = authentication.getName();
        UserEntity currentUser = userRepository.findByEmail(currentUsername).orElseThrow();

        // Get users you sent messages to
        List<UserEntity> sentTo = singleRepository.findDistinctReceiversBySender(currentUser);

        // Get users who sent messages to you
        List<UserEntity> receivedFrom = singleRepository.findDistinctSendersByReceiver(currentUser);

        // Combine and remove duplicates
        Set<UserEntity> contacts = new HashSet<>();
        contacts.addAll(sentTo);
        contacts.addAll(receivedFrom);

        // Convert to DTOs
        List<profileResponseDto> contactDtos = contacts.stream()
                .map(profileResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(contactDtos);
    }

    @DeleteMapping("/api/chats/delete/{otherUserEmail}")
    public ResponseEntity<?> deleteChatWithUser(@PathVariable String otherUserEmail, Authentication authentication) {
        String currentUsername = authentication.getName();

        UserEntity currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        UserEntity otherUser = userRepository.findByEmail(otherUserEmail)
                .orElseThrow(() -> new RuntimeException("User to delete chat with not found"));

        List<SingleMessage> messagesSent = singleRepository.findBySenderAndReceiver(currentUser, otherUser);
        List<SingleMessage> messagesReceived = singleRepository.findBySenderAndReceiver(otherUser, currentUser);

        singleRepository.deleteAll(messagesSent);
        singleRepository.deleteAll(messagesReceived);

        return ResponseEntity.ok("Chat deleted successfully.");
    }


}