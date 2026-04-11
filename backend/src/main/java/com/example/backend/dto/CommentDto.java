package com.example.backend.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Long toUserId;//接受者（被评论失物、拾取的发布者，或私聊的对象）
    private Integer targetType;
    private Long targetId;
    private String content;
}
