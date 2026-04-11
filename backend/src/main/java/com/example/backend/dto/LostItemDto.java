package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LostItemDto {
    private String name;
    private String location;
    private LocalDateTime lostTime;
    private String description;
    private String imageUrl;
    private String contact;
}