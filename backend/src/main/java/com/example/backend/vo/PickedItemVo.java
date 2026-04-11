package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PickedItemVo {
    private Long id;
    private Long userId;
    private String name;
    private String location;
    private LocalDateTime pickTime;
    private String description;
    private String aiDescription;
    private String imageUrl;
    private String contact;
    private Integer visibilityPreset;
    private Integer enableClaimWorkflow;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}