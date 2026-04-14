package com.example.backend.service.impl;

import com.example.backend.service.AiService;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiServiceImpl implements AiService {
    @Autowired
    private ChatClient chatClient;

    @Override
    public String generateItemDescription(String itemName, String userDescription) {
        String prompt = "你是一个失物招领助手。用户捡到物品：[" + itemName + "]，用户描述：[" +
                (userDescription == null ? "无" : userDescription) + "]。" +
                "请生成一段更详细的物品描述（20字以内），帮助失主识别。只输出描述，不要多余解释。";
        ChatResponse response = chatClient.call(new Prompt(prompt));
        String result = response.getResult().getOutput().getContent();
        if (result != null) {
            result = result.trim().replaceAll("^\"|\"$", "");
        }
        return result;
    }
}