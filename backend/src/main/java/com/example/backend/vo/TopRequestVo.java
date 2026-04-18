package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TopRequestVo {//管理员查看的
    private Long id;
    private Long itemId;
    private Integer itemType;
    private Long userId;
    private String username; //申请人用户名
    private String itemName; //物品名称
    private Integer status;
    private LocalDateTime requestTime;
    private Long adminId;
    private LocalDateTime approveTime;
}