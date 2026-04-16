package com.example.backend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
public class PickedItemDto {
    private String name;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")//添加@JsonFormat注解，解决日期格式解析错误。
    private LocalDateTime pickTime;
    private String description;
    private String imageUrl;
    private String contact;
    private Integer visibilityPreset;
    private Integer enableClaimWorkflow;
}