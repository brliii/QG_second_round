package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LostItemVo {
    private Long id;
    private Long userId;
    private String name;
    private String location;
    private LocalDateTime lostTime;
    private String description;
    private String aiDescription;
    private String imageUrl;
    private String contact;
    private Integer isTop;
    private LocalDateTime topExpire;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}