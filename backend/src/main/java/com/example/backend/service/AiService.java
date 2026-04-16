package com.example.backend.service;

import com.example.backend.entity.PickedItem;
import com.example.backend.vo.PickedItemVo;
import com.example.backend.vo.SearchResultVo;

import java.util.List;

public interface AiService {
    //AI生成描述
    String generateItemDescription(String itemName, String userDescription);
    //AI根据用户描述智能查找失物
    List<PickedItem> searchBestMatches(String userDescription, List<PickedItem> candidates);
}