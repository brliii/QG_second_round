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
    private Integer status;
    private String pickerComment;
    private String pickupCode;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}