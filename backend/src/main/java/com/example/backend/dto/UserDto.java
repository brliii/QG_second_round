package com.example.backend.dto;

import lombok.Data;

import java.awt.event.PaintEvent;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer status;
    private Integer role;
    private LocalDateTime createTime;
}
