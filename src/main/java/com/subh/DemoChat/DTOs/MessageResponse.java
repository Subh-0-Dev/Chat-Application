package com.subh.DemoChat.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.subh.DemoChat.Entity.Room;
import com.subh.DemoChat.Entity.UserEntity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private Long id;
    private String content;
    private LocalDateTime timeStamp;
    private String roomname;
    private String username;
}
