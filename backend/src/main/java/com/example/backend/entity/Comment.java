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
    private Integer targetType;//0-失物评论 1-拾取评论 2-私聊
    private Long targetId;//失物、拾取id，私聊为0
    private String content;
    private Integer isRead;//0-未读 1-已读
    private Long parentId;
    private LocalDateTime createTime;
}