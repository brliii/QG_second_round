package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("claim_request")
public class ClaimRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long pickedItemId;
    private Long claimantId;
    private String verifyAnswer;
    private Integer status; //0-待审核 1-同意 2-拒绝 3-要求补充证据 4-已补充证据
    private String pickerComment;
    private String pickupCode;
    private String evidence; //失主补充的证据
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}