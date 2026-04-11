package com.example.backend.dto;

import lombok.Data;

@Data
public class ReportDto {
    private Integer targetType;
    private Long targetId;
    private String reason;
}
