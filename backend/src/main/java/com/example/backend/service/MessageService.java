package com.example.backend.service;

import com.example.backend.entity.Message;

import java.util.List;

public interface MessageService {
    boolean create(Message message);
    List<Message> getByConversationId(Long conversationId, Integer page, Integer size);
    Message getLastMessageByConversationId(Long conversationId);
}
