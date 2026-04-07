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

}
