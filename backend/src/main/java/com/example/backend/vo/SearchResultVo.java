package com.example.backend.vo;

import com.example.backend.dto.SearchRequestDto;
import lombok.Data;

@Data
public class SearchResultVo {//进阶AI，根据用户描述智能寻找失物
    private Long id;
    private String name;
    private String description;
    private String location;
    private String lostTime;
    private Double relevanceScore;//相关性分数，越高越相关，用于排序
}
