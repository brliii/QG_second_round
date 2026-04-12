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
    private Integer itemType;//0-失物 1-拾取
    private Long userId;
    private Integer status;//0-待审核 1-同意置顶 2-拒绝置顶
    private LocalDateTime requestTime;//用户发起申请的时刻
    private Long adminId;
    private LocalDateTime approveTime;//管理员审核完成的时刻
}