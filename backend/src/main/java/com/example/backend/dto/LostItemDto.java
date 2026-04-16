package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LostItemDto {
    private String name;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")//添加@JsonFormat注解，指定日期格式为"yyyy-MM-dd HH:mm:ss"，以解决前端传递的日期字符串无法解析的问题。
    private LocalDateTime lostTime;
    private String description;
    private String imageUrl;
    private String contact;
}