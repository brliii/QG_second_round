package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private Integer targetType;
    private Long targetId;
    private String reason;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}