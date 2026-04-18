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
    private Integer status; //0-待审核 1-同意 2-拒绝 3-要求补充证据 4-已补充证据
    private String pickerComment;
    private String pickupCode;
    private String evidence; //失主补充的证据
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}