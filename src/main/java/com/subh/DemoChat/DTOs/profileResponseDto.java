package com.subh.DemoChat.DTOs;

import com.subh.DemoChat.Entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class profileResponseDto {
    Long id;
    String username;
    String email;

    public profileResponseDto(UserEntity user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }


}
