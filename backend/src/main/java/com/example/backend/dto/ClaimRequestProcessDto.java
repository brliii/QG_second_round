package com.example.backend.dto;

import lombok.Data;

@Data
public class ClaimRequestProcessDto {//处理认领申请
    private Integer status; //1-同意 2-拒绝 3-要求补充证据
    private String comment; //拾取者的回复
}