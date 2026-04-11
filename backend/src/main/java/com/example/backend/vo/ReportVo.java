package com.example.backend.vo;

import lombok.Data;

import java.security.PrivateKey;
import java.time.LocalDateTime;

@Data
public class ReportVo {
    private Long id;
    private Long reportId;
    private String reporterUsername;//举报者名
    private Integer targetType;
    private Long targetId;
    private String reason;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
