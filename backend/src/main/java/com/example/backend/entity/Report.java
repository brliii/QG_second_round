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
    private Integer targetType;//0-失物 1-拾取 2-评论 3-用户
    private Long targetId;
    private String reason;
    private Integer status;//0-待审核 1-已驳回 2-已删除内容(失物、拾取、评论) 3-已封禁用户
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}