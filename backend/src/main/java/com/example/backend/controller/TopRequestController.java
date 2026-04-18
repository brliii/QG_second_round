package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.TopRequestCreateDto;
import com.example.backend.entity.LostItem;
import com.example.backend.entity.PickedItem;
import com.example.backend.entity.TopRequest;
import com.example.backend.entity.User;
import com.example.backend.service.LostItemService;
import com.example.backend.service.PickedItemService;
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
    private PickedItemService pickedItemService;
    @Autowired
    private UserService userService;

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody TopRequestCreateDto dto, HttpServletRequest request){
        Long userId=(Long) request.getAttribute("userId");
        if(userId==null){
            return Result.error(401,"未登录");
        }
        
        // 检查物品类型和所有权
        boolean isOwner = false;
        if (dto.getItemType() == 0) {
            // 失物
            isOwner = lostItemService.isOwner(userId, dto.getItemId());
        } else if (dto.getItemType() == 1) {
            // 拾取
            isOwner = pickedItemService.isOwner(userId, dto.getItemId());
        } else {
            return Result.error(400, "无效的物品类型");
        }
        
        if (!isOwner) {
            return Result.error(403, "物品不存在或不属于你");
        }

        TopRequest topRequest = new TopRequest();
        topRequest.setItemId(dto.getItemId());
        topRequest.setItemType(dto.getItemType());
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
            // 根据物品类型和物品ID查询物品名称
            if (tr.getItemType() != null && tr.getItemId() != null) {
                String itemName = null;
                if (tr.getItemType() == 0) {
                    // 失物
                    itemName = lostItemService.getById(tr.getItemId()).getName();
                } else if (tr.getItemType() == 1) {
                    // 拾取
                    itemName = pickedItemService.getById(tr.getItemId()).getName();
                }
                vo.setItemName(itemName);
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
            if (topRequest.getItemType() == 0) {
                // 失物
                LostItem item=lostItemService.getById(topRequest.getItemId());
                if(item!=null){
                    item.setIsTop(1);
                    item.setTopExpire(expireTime);
                    lostItemService.adminUpdate(item);
                }
            } else if (topRequest.getItemType() == 1) {
                // 拾取
                PickedItem item=pickedItemService.getById(topRequest.getItemId());
                if(item!=null){
                    item.setIsTop(1);
                    item.setTopExpire(expireTime);
                    pickedItemService.adminUpdate(item);
                }
            }
        }

        boolean success = topRequestService.approve(requestId, adminId, approveStatus);
        return success ? Result.success("处理成功") : Result.error(403, "无权操作或处理失败");
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestBody TopRequestCreateDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 检查物品类型和所有权
        boolean isOwner = false;
        if (dto.getItemType() == 0) {
            // 失物
            isOwner = lostItemService.isOwner(userId, dto.getItemId());
        } else if (dto.getItemType() == 1) {
            // 拾取
            isOwner = pickedItemService.isOwner(userId, dto.getItemId());
        } else {
            return Result.error(400, "无效的物品类型");
        }

        if (!isOwner) {
            return Result.error(403, "物品不存在或不属于你");
        }

        // 取消置顶
        if (dto.getItemType() == 0) {
            // 失物
            LostItem item = lostItemService.getById(dto.getItemId());
            if (item != null) {
                item.setIsTop(0);
                item.setTopExpire(null);
                lostItemService.adminUpdate(item);
            }
        } else if (dto.getItemType() == 1) {
            // 拾取
            PickedItem item = pickedItemService.getById(dto.getItemId());
            if (item != null) {
                item.setIsTop(0);
                item.setTopExpire(null);
                pickedItemService.adminUpdate(item);
            }
        }

        return Result.success("置顶已取消");
    }
}