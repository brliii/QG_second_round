package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVo {
    private Long id;
    private Long fromUserId;
    private String fromUsername;//留言者用户名（需要额外查询）
    private Long toUserId;
    private String toUsername;//接收者用户名
    private Integer targetType;
    private Long targetId;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}
