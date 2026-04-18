package com.example.backend.dto;

import lombok.Data;

@Data
public class TopRequestCreateDto {//用户提交的
    private Long itemId;
    private Integer itemType;//0-失物 1-拾取
}