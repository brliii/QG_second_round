package com.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PickedItemDto {
    private String name;
    private String location;
    private LocalDateTime pickTime;
    private String description;
    private String imageUrl;
    private String contact;
    private Integer visibilityPreset;
    private Integer enableClaimWorkflow;
}