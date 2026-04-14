package com.example.backend.dto;

import lombok.Data;

@Data
public class ClaimRequestCreateDto {//发起认领申请
    private Long pickedItemId;
    private String verifyAnswer;
}