package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lost_item")
public class LostItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String location;
    private LocalDateTime lostTime;
    private String description;
    private String aiDescription;
    private String imageUrl;
    private String contact;
    private Integer isTop;//1-置顶
    private LocalDateTime topExpire;//置顶失效时间
    private Integer status;//0-正常 1-已找回 2-已删除（逻辑删除）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
