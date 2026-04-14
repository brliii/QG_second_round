package com.example.backend.dto;

import lombok.Data;

@Data
public class UpdateUserInfoDto {
    private String nickname;   //昵称，可选
    private String avatar;     //头像URL，可选
    private String phone;      //可选
}