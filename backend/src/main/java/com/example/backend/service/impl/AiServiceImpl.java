package com.example.backend.service.impl;

import com.example.backend.entity.PickedItem;
import com.example.backend.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    @Value("${zhipu.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String ZHIPU_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    @Override
    public String generateItemDescription(String itemName, String userDescription) {
        String prompt = "你是一个失物招领助手。用户捡到物品：「" + itemName + "」，用户描述：" +
                (userDescription == null ? "无" : userDescription) +
                "。请生成一段更详细的物品描述（20字以内），帮助失主识别。只输出描述，不要多余解释。";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "glm-4-flash");
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(ZHIPU_URL, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    if (content != null && !content.trim().isEmpty()) {
                        return content.trim().replaceAll("^\"|\"$", "");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 备用描述
        return itemName + "，物品特征明显，请尽快联系失主。";
    }

    // 其他方法（如 searchBestMatches）如果也需要 AI 调用，请类似改造，或者暂时返回空列表
    @Override
    public List<PickedItem> searchBestMatches(String userDescription, List<PickedItem> candidates) {
        // 暂时返回原顺序，后续可按需实现
        return candidates;
    }
}