package com.subh.DemoChat.DTOs;

import com.subh.DemoChat.Entity.UserEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SingleResponse {
    private Long id;
    private String content;
    private LocalDateTime timeStamp;
    private UserEntity sender;
    private UserEntity receiver;
}
