package com.example.backend.dto;

import lombok.Data;

@Data
public class UserRegisterDto {
    private String username;
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;  // 新增
}