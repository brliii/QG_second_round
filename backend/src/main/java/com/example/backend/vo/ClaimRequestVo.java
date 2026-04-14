package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClaimRequestVo {
    private Long id;
    private Long pickedItemId;
    private String pickedItemName;   //物品名称
    private Long claimantId;
    private String claimantUsername; //失主用户名
    private String verifyAnswer;
    private Integer status;
    private String pickerComment;
    private String pickupCode;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}