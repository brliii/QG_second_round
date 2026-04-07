package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Integer targetType;
    private Long targetId;
    private String content;
    private Integer isRead;
    private Long parentId;
    private LocalDateTime createTime;
}