package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("top_request")
public class TopRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;
    private Integer itemType;
    private Long userId;
    private Integer status;
    private LocalDateTime requestTime;
    private Long adminId;
    private LocalDateTime approveTime;
}