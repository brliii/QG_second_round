package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.Conversation;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.service.ConversationService;
import com.example.backend.service.MessageService;
import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        List<Conversation> conversations = conversationService.getByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Conversation conversation : conversations) {
            Map<String, Object> conversationMap = new HashMap<>();
            conversationMap.put("id", conversation.getId());

            // 获取对方用户信息
            Long otherUserId = conversation.getUserId1().equals(userId) ? conversation.getUserId2() : conversation.getUserId1();
            User otherUser = userService.getById(otherUserId);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", otherUser.getId());
            userMap.put("username", otherUser.getUsername());
            conversationMap.put("otherUser", userMap);

            // 获取最后一条消息
            Message lastMessage = messageService.getLastMessageByConversationId(conversation.getId());
            if (lastMessage != null) {
                conversationMap.put("lastMessage", lastMessage.getContent());
                conversationMap.put("lastMessageTime", lastMessage.getSendTime());
            }

            // 暂时设置未读消息数为0
            conversationMap.put("unreadCount", 0);

            result.add(conversationMap);
        }

        return Result.success(result);
    }

    @GetMapping("/conversation")
    public Result<Map<String, Object>> getOrCreateConversation(@RequestParam Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }

        if (currentUserId.equals(userId)) {
            return Result.error(400, "不能与自己聊天");
        }

        // 获取或创建会话
        Conversation conversation = conversationService.getOrCreate(currentUserId, userId);

        // 获取对方用户信息
        Long otherUserId = conversation.getUserId1().equals(currentUserId) ? conversation.getUserId2() : conversation.getUserId1();
        User otherUser = userService.getById(otherUserId);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", conversation.getId());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", otherUser.getId());
        userMap.put("username", otherUser.getUsername());
        result.put("otherUser", userMap);

        // 获取消息历史
        List<Message> messages = messageService.getByConversationId(conversation.getId(), 1, 50);
        List<Map<String, Object>> messageList = new ArrayList<>();
        for (Message message : messages) {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("senderId", message.getSenderId());
            messageMap.put("content", message.getContent());
            messageMap.put("sendTime", message.getSendTime());
            // 添加发送者信息
            User sender = userService.getById(message.getSenderId());
            if (sender != null) {
                messageMap.put("senderName", sender.getUsername());
            }
            messageList.add(messageMap);
        }
        result.put("messages", messageList);

        return Result.success(result);
    }

    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            Long conversationId = Long.valueOf(requestBody.get("conversationId").toString());
            String content = requestBody.get("content").toString();

            // 验证会话是否存在
            List<Conversation> userConversations = conversationService.getByUserId(userId);
            boolean conversationExists = false;
            for (Conversation c : userConversations) {
                if (c.getId().equals(conversationId)) {
                    conversationExists = true;
                    break;
                }
            }
            if (!conversationExists) {
                return Result.error(404, "会话不存在");
            }

            // 创建消息
            Message message = new Message();
            message.setConversationId(conversationId);
            message.setSenderId(userId);
            message.setContent(content);

            boolean success = messageService.create(message);
            if (!success) {
                return Result.error(500, "发送消息失败");
            }

            // 更新会话最后消息时间
            conversationService.updateLastMessageTime(conversationId);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", message.getId());
            result.put("senderId", message.getSenderId());
            result.put("content", message.getContent());
            result.put("sendTime", message.getSendTime());
            // 添加发送者信息
            User sender = userService.getById(message.getSenderId());
            if (sender != null) {
                result.put("senderName", sender.getUsername());
            }

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "发送消息失败：" + e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getMessages(@RequestParam Long conversationId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer size, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        List<Message> messages = messageService.getByConversationId(conversationId, page, size);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Message message : messages) {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("senderId", message.getSenderId());
            messageMap.put("content", message.getContent());
            messageMap.put("sendTime", message.getSendTime());
            // 添加发送者信息
            User sender = userService.getById(message.getSenderId());
            if (sender != null) {
                messageMap.put("senderName", sender.getUsername());
            }
            result.add(messageMap);
        }

        return Result.success(result);
    }
}
