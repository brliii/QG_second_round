package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Conversation;
import com.example.backend.mapper.ConversationMapper;
import com.example.backend.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {
    @Autowired
    private ConversationMapper conversationMapper;

    @Override
    public List<Conversation> getByUserId(Long userId) {
        QueryWrapper<Conversation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id1", userId).or().eq("user_id2", userId);
        return conversationMapper.selectList(wrapper);
    }

    @Override
    public Conversation getOrCreate(Long userId1, Long userId2) {
        // 确保userId1 < userId2，避免重复创建会话
        if (userId1 > userId2) {
            Long temp = userId1;
            userId1 = userId2;
            userId2 = temp;
        }

        // 查找是否已存在会话
        QueryWrapper<Conversation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id1", userId1).eq("user_id2", userId2);
        Conversation conversation = conversationMapper.selectOne(wrapper);
        if (conversation != null) {
            return conversation;
        }

        // 创建新会话
        conversation = new Conversation();
        conversation.setUserId1(userId1);
        conversation.setUserId2(userId2);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public boolean updateLastMessageTime(Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            conversation.setLastMessageTime(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());
            return conversationMapper.updateById(conversation) > 0;
        }
        return false;
    }
}
