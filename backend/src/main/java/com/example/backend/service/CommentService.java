package com.example.backend.service;

import com.example.backend.entity.Comment;
import java.util.List;

public interface CommentService {
    //发评论
    boolean send(Comment comment);
    //查找目标物品的评论
    List<Comment> getCommentsByTarget(Integer targetType, Long targetId);
    //查找私聊信息
    List<Comment> getPrivateMessages(Long toUserId);
    //查询未读信息数
    long getUnreadCount(Long userId);
    //标记未读为已读信息
    boolean markAsRead(Long commentId, Long userId);
    //根据id删除评论（需管理员权限）
    boolean deleteById(Long adminId, Long id);
    //根据id获取评论详情
    Comment getById(Long id);
}