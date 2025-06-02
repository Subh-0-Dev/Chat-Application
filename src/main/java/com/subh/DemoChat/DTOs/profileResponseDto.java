package com.subh.DemoChat.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class profileResponseDto {
    Long id;
    String username;
    String email;
}
