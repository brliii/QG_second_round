package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.TopRequestCreateDto;
import com.example.backend.entity.LostItem;
import com.example.backend.entity.TopRequest;
import com.example.backend.entity.User;
import com.example.backend.service.LostItemService;
import com.example.backend.service.TopRequestService;
import com.example.backend.service.UserService;
import com.example.backend.service.impl.UserServiceImpl;
import com.example.backend.utils.ConvertUtil;
import com.example.backend.vo.TopRequestVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/top")
public class TopRequestController {
    @Autowired
    private TopRequestService topRequestService;
    @Autowired
    private LostItemService lostItemService;
    @Autowired
    private UserService userService;

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody TopRequestCreateDto dto, HttpServletRequest request){
        Long userId=(Long) request.getAttribute("userId");
        if(userId==null){
            return Result.error(401,"未登录");
        }
        //检验失物是不是当前用户的
        if (!lostItemService.isOwner(userId, dto.getItemId())) {
            return Result.error(403, "失物不存在或不属于你");
        }

        TopRequest topRequest = new TopRequest();
        topRequest.setItemId(dto.getItemId());
        topRequest.setItemType(0);//固定为失物
        topRequest.setUserId(userId);
        boolean success = topRequestService.create(topRequest);
        return success?Result.success("申请已提交，等待管理员审批"):Result.error(500, "申请失败");
    }

    @GetMapping("/pending")
    public Result<List<TopRequestVo>> getPending(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        if (adminId == null) {
            return Result.error(401, "未登录");
        }
        List<TopRequest> list = topRequestService.getPendingRequests(adminId);
        if (list == null) {
            return Result.error(403, "仅管理员可访问");
        }
        List<TopRequestVo> voList = new ArrayList<>();
        for (TopRequest tr : list) {
            TopRequestVo vo = ConvertUtil.convert(tr, TopRequestVo.class);
            User user = userService.getCurrentUser(tr.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @PutMapping("/approve/{requestId}")
    public Result<String> approve(@PathVariable Long requestId,@RequestParam Integer approveStatus,HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        if (adminId == null) {
            return Result.error(401, "未登录");
        }
        TopRequest topRequest = topRequestService.getById(requestId);
        if (topRequest == null) {
            return Result.error(404, "申请不存在");
        }

        //同意申请则置顶，不同意则无需操作
        if(approveStatus==1){
            LocalDateTime expireTime=LocalDateTime.now().plusHours(24);
            LostItem item=lostItemService.getById(topRequest.getItemId());
            if(item!=null){
                item.setIsTop(1);
                item.setTopExpire(expireTime);
                lostItemService.adminUpdate(item);
            }
        }

        boolean success = topRequestService.approve(requestId, adminId, approveStatus);
        return success ? Result.success("处理成功") : Result.error(403, "无权操作或处理失败");
    }
}