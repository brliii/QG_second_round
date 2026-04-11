package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("picked_item")
public class PickedItem {
    @TableId(type= IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String location;
    private LocalDateTime pickTime;
    private String description;
    private String aiDescription;
    private String imageUrl;
    private String contact;
    private Integer visibilityPreset;//信息分级（针对的是除了自己和管理员之外的其他人）  0-公开 1-隐藏联系方式 2-仅注册用户可见全部 3-仅自己可见
    private Integer enableClaimWorkflow;//是否启动认领申请
    private Integer status;//0-正常 1-已认领 2-已删除（逻辑删除）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
