package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.CommentDto;
import com.example.backend.dto.CommentDto;
import com.example.backend.entity.Comment;
import com.example.backend.entity.User;
import com.example.backend.service.CommentService;
import com.example.backend.service.UserService;
import com.example.backend.utils.ConvertUtil;
import com.example.backend.vo.CommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;

    @PostMapping("/send")
    public Result<String> send(@RequestBody CommentDto dto, HttpServletRequest request) {
        Long fromUserId = (Long) request.getAttribute("userId");
        if (fromUserId == null) {
            return Result.error(401, "未登录");
        }
        Comment comment = new Comment();
        comment.setFromUserId(fromUserId);
        comment.setToUserId(dto.getToUserId() != null ? dto.getToUserId() : 0L);
        comment.setTargetType(dto.getTargetType());
        comment.setTargetId(dto.getTargetId());
        comment.setContent(dto.getContent());
        boolean success = commentService.send(comment);
        return success ? Result.success("发送成功") : Result.error(500, "发送失败");
    }

    @GetMapping("/target")
    public Result<List<CommentVo>> getCommentsByTarget(@RequestParam Integer targetType, @RequestParam Long targetId) {
        List<Comment> comments = commentService.getCommentsByTarget(targetType, targetId);
        List<CommentVo> voList = new ArrayList<>();
        for (Comment c : comments) {
            CommentVo vo = ConvertUtil.convert(c, CommentVo.class);
            //补充留言者和接收者用户名（因为在service层返回的是comment的entity，里面不包含名字）
            User fromUser = userService.getCurrentUser(c.getFromUserId());
            if (fromUser != null) {
                vo.setFromUsername(fromUser.getUsername());
            }
            User toUser = userService.getCurrentUser(c.getToUserId());
            if (toUser != null) {
                vo.setToUsername(toUser.getUsername());
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @GetMapping("/private")
    public Result<List<CommentVo>> getPrivateMessages(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if(userId == null) {
            return Result.error(401,"未登录");
        }
        List<Comment> messages = commentService.getPrivateMessages(userId);
        List<CommentVo> voList = new ArrayList<>();

        for (Comment c : messages) {//有多条数据,加一个名字返回给前端
            CommentVo vo=ConvertUtil.convert(c, CommentVo.class);
            User fromUser = userService.getCurrentUser(c.getFromUserId());
            if (fromUser != null) {
                vo.setFromUsername(fromUser.getUsername());
            }
            User toUser = userService.getCurrentUser(c.getToUserId());
            if (toUser != null) {
                vo.setToUsername(toUser.getUsername());
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId= (Long) request.getAttribute("userId");
        if(userId == null) {
            return Result.error(401,"未登录");
        }
        long count = commentService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id,HttpServletRequest request){//这里的id是commentId
        Long userId=(Long) request.getAttribute("userId");
        if(userId == null) {
            return Result.error(401,"未登录");
        }
        boolean success = commentService.markAsRead(id,userId);
        return success?Result.success("已标记为已读"):Result.error(500,"操作失败");
    }

    @DeleteMapping("/admin/delete/{id}")
    public Result<String> adminDeleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        if (adminId == null) {
            return Result.error(401, "未登录");
        }
        boolean success = commentService.deleteById(adminId, id);
        return success ? Result.success("删除成功") : Result.error(403, "无权操作或删除失败");
    }

    @GetMapping("/admin/detail/{id}")
    public Result<CommentVo> getCommentDetail(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        if (adminId == null) {
            return Result.error(401, "未登录");
        }
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        CommentVo vo = ConvertUtil.convert(comment, CommentVo.class);
        // 补充留言者和接收者用户名
        User fromUser = userService.getCurrentUser(comment.getFromUserId());
        if (fromUser != null) {
            vo.setFromUsername(fromUser.getUsername());
        }
        User toUser = userService.getCurrentUser(comment.getToUserId());
        if (toUser != null) {
            vo.setToUsername(toUser.getUsername());
        }
        return Result.success(vo);
    }
}