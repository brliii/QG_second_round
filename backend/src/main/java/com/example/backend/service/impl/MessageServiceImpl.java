package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.Message;
import com.example.backend.mapper.MessageMapper;
import com.example.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public boolean create(Message message) {
        message.setSendTime(LocalDateTime.now());
        message.setCreatedAt(LocalDateTime.now());
        return messageMapper.insert(message) > 0;
    }

    @Override
    public List<Message> getByConversationId(Long conversationId, Integer page, Integer size) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId);
        wrapper.orderByDesc("send_time");
        wrapper.last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        return messageMapper.selectList(wrapper);
    }

    @Override
    public Message getLastMessageByConversationId(Long conversationId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("conversation_id", conversationId);
        wrapper.orderByDesc("send_time");
        wrapper.last("LIMIT 1");
        return messageMapper.selectOne(wrapper);
    }
}
