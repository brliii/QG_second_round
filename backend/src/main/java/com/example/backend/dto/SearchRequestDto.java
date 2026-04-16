package com.example.backend.dto;

import lombok.Data;
import org.springframework.web.servlet.view.script.ScriptTemplateConfig;

@Data
public class SearchRequestDto {//进阶AI，基于用户描述智能搜索失物
    private String description;
}
