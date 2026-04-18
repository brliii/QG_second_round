package com.example.backend.service;

import com.example.backend.entity.Conversation;

import java.util.List;

public interface ConversationService {
    List<Conversation> getByUserId(Long userId);
    Conversation getOrCreate(Long userId1, Long userId2);
    boolean updateLastMessageTime(Long conversationId);
}
