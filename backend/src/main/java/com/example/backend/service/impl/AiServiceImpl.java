package com.example.backend.service.impl;

import com.example.backend.entity.LostItem;
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
                "。请生成一段更详细的物品描述（需要对物品进行智能识别分类）（20字以内），帮助失主识别。只输出分类和描述，先输出分类再输出描述，不要多余解释。";

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


    @Override
    public List<PickedItem> searchBestMatches(String userDescription, List<PickedItem> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建AI请求，计算每个物品与用户描述的匹配度
        String prompt = "你是一个失物招领匹配助手。请根据用户的描述，找出最匹配的物品。\n\n"
                + "注意：用户输入可能是简称、口语化描述或部分特征，如'黑的'可能指'黑色'、'黑皮'等。请根据语义理解进行匹配，不要仅匹配文字完全相同。\n\n"
                + "用户描述：\"" + userDescription + "\"\n\n"
                + "物品列表：\n";

        for (int i = 0; i < candidates.size(); i++) {
            PickedItem item = candidates.get(i);
            prompt += (i + 1) + ". 物品名称：" + item.getName() + "，描述：" + (item.getDescription() != null ? item.getDescription() : "无") + "\n";
        }

        prompt += "\n请返回一个JSON数组，其中每个元素包含物品索引（从1开始）和匹配度（0-100），按匹配度从高到低排序。匹配度要充分考虑语义相似性和特征匹配。格式如下：\n"
                + "[{\"index\": 1, \"score\": 95}, {\"index\": 2, \"score\": 80}, ...]";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "glm-4-flash");
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3); // 降低随机性，提高一致性

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
                        // 解析JSON响应
                        // 注意：这里需要处理可能的JSON格式问题
                        content = content.trim().replaceAll("^```json|```$", "");
                        try {
                            // 简单的JSON解析
                            org.json.JSONArray jsonArray = new org.json.JSONArray(content);
                            List<PickedItem> sortedItems = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int index = jsonObject.getInt("index") - 1; // 转换为0-based索引
                                if (index >= 0 && index < candidates.size()) {
                                    sortedItems.add(candidates.get(index));
                                }
                            }
                            return sortedItems;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果AI调用失败，返回原列表
        return candidates;
    }

    @Override
    public List<LostItem> searchLostBestMatches(String userDescription, List<LostItem> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建AI请求，计算每个物品与用户描述的匹配度
        String prompt = "你是一个失物招领匹配助手。请根据用户的描述，找出最匹配的物品。\n\n"
                + "用户描述：\"" + userDescription + "\"\n\n"
                + "注意：用户输入可能是简称、口语化描述或部分特征，如\"黑的\"可能指\"黑色\"、\"黑皮\"等，\"校园卡\"可能指\"一卡通\"等。请理解这些语义相似性。\n\n"
                + "失物列表：\n";

        for (int i = 0; i < candidates.size(); i++) {
            LostItem item = candidates.get(i);
            prompt += (i + 1) + ". 物品名称：" + item.getName() + "，描述：" + (item.getDescription() != null ? item.getDescription() : "无") + "\n";
        }

        prompt += "\n请返回一个JSON数组，其中每个元素包含物品索引（从1开始）和匹配度（0-100），按匹配度从高到低排序。匹配度要充分考虑语义相似性和特征匹配。格式如下：\n"
                + "[{\"index\": 1, \"score\": 95}, {\"index\": 2, \"score\": 80}, ...]";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "glm-4-flash");
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3); // 降低随机性，提高一致性

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
                        // 解析JSON响应
                        // 注意：这里需要处理可能的JSON格式问题
                        content = content.trim().replaceAll("^```json|```$", "");
                        try {
                            // 简单的JSON解析
                            org.json.JSONArray jsonArray = new org.json.JSONArray(content);
                            List<LostItem> sortedItems = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int index = jsonObject.getInt("index") - 1; // 转换为0-based索引
                                if (index >= 0 && index < candidates.size()) {
                                    sortedItems.add(candidates.get(index));
                                }
                            }
                            return sortedItems;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果AI调用失败，返回原列表
        return candidates;
    }
}