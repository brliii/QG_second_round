package com.example.backend.service;

public interface AiService {
    //AI生成描述
    String generateItemDescription(String itemName, String userDescription);
}