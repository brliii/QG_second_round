package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Comment;
import com.example.backend.entity.User;
import com.example.backend.mapper.CommentMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean send(Comment comment) {
        comment.setIsRead(0);
        comment.setParentId(null);
        return commentMapper.insert(comment) > 0;
    }

    @Override
    public List<Comment> getCommentsByTarget(Integer targetType, Long targetId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("target_type", targetType).eq("target_id", targetId)
                .orderByDesc("create_time");
        return commentMapper.selectList(wrapper);
    }

    @Override
    public List<Comment> getPrivateMessages(Long toUserId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("target_type", 2).eq("to_user_id", toUserId)
                .orderByDesc("create_time");
        return commentMapper.selectList(wrapper);
    }

    @Override
    public long getUnreadCount(Long userId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", userId).eq("is_read", 0);
        return commentMapper.selectCount(wrapper);
    }

    @Override
    public boolean markAsRead(Long commentId, Long userId) {//userId是从控制层的请求传进来的，相当于作为验证信息了
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("id", commentId).eq("to_user_id", userId);
        Comment update = new Comment();
        update.setIsRead(1);
        return commentMapper.update(update, wrapper) > 0;
    }

    @Override
    public boolean deleteById(Long adminId, Long id) {
        User admin = userMapper.selectById(adminId);
        if (admin == null || admin.getRole() != 1) {
            return false;
        }
        //这块是直接物理删除，因为表里没有status字段
        return commentMapper.deleteById(id) > 0;
    }
}