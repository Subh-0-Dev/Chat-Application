package com.subh.DemoChat.Service;

import com.subh.DemoChat.DTOs.SingleResponse;
import com.subh.DemoChat.Entity.SingleMessage;
import com.subh.DemoChat.Entity.UserEntity;
import com.subh.DemoChat.Repository.SingleRepository;
import com.subh.DemoChat.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SingleService {

    @Autowired
    private SingleRepository singleRepository;

    @Autowired
    private UserRepository userRepository;

    public SingleMessage sendMessage(String senderUsername, String receiverUsername, String content) {
        // Try to find sender by username first, then by email if not found
        Optional<UserEntity> senderOpt = userRepository.findByUsername(senderUsername);
        if (senderOpt.isEmpty()) {
            senderOpt = userRepository.findByEmail(senderUsername);
        }

        Optional<UserEntity> receiverOpt = userRepository.findByUsername(receiverUsername);
        if (receiverOpt.isEmpty()) {
            receiverOpt = userRepository.findByEmail(receiverUsername);
        }

        if (senderOpt.isEmpty()) {
            throw new RuntimeException("Sender not found: " + senderUsername);
        }
        if (receiverOpt.isEmpty()) {
            throw new RuntimeException("Receiver not found: " + receiverUsername);
        }

        UserEntity sender = senderOpt.get();
        UserEntity receiver = receiverOpt.get();

        System.out.println(senderUsername);
        System.out.println(receiverUsername);

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Cannot send message to yourself");
        }

        SingleMessage message = new SingleMessage();
        message.setMessage(content);
        message.setSender(sender);
        message.setReceiver(receiver);

        return singleRepository.save(message);
    }

    public List<SingleResponse> getMessagesBetweenUsers(String username1, String username2) {
        // Try to find users by username first, then by email if not found
        Optional<UserEntity> user1Opt = userRepository.findByUsername(username1);
        if (user1Opt.isEmpty()) {
            user1Opt = userRepository.findByEmail(username1);
        }

        Optional<UserEntity> user2Opt = userRepository.findByUsername(username2);
        if (user2Opt.isEmpty()) {
            user2Opt = userRepository.findByEmail(username2);
        }

        if (user1Opt.isEmpty()) {
            throw new RuntimeException("User not found: " + username1);
        }
        if (user2Opt.isEmpty()) {
            throw new RuntimeException("User not found: " + username2);
        }

        UserEntity user1 = user1Opt.get();
        UserEntity user2 = user2Opt.get();

        List<SingleMessage> messages = singleRepository.findChatBetweenUsers(user1,user2);

        List<SingleResponse> messageList=new ArrayList<>();

        for (SingleMessage message:messages){
            SingleResponse messagedto=new SingleResponse();
            messagedto.setId(message.getMessageId());
            messagedto.setContent(message.getMessage());
            messagedto.setSender(message.getSender());
            messagedto.setReceiver(message.getReceiver());
            messagedto.setTimeStamp(message.getTimestamp());
            messageList.add(messagedto);
        }

        return messageList;
    }

    // Helper method to get user by username or email
    public Optional<UserEntity> findUserByUsernameOrEmail(String identifier) {
        Optional<UserEntity> user = userRepository.findByUsername(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(identifier);
        }
        return user;
    }
}