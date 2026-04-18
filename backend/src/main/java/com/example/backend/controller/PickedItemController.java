package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.Result;
import com.example.backend.dto.PickedItemDto;
import com.example.backend.entity.PickedItem;
import com.example.backend.enums.VisibilityPresetEnum;
import com.example.backend.service.AiService;
import com.example.backend.service.PickedItemService;
import com.example.backend.service.UserService;
import com.example.backend.utils.ConvertUtil;
import com.example.backend.vo.PickedItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/picked")
public class PickedItemController {
    @Autowired
    private PickedItemService pickedItemService;
    @Autowired
    private AiService aiService;

    @PostMapping("/create")
    public Result<Long> create(@RequestBody PickedItemDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PickedItem item = new PickedItem();//service层创建方法需要一个实体
        //这里存在字段名不一致，所以不能直接用转换工具
        item.setName(dto.getName());
        item.setLocation(dto.getLocation());
        item.setPickTime(dto.getPickTime());
        item.setDescription(dto.getDescription());
        item.setImageUrl(dto.getImageUrl());
        item.setContact(dto.getContact());
        item.setVisibilityPreset(dto.getVisibilityPreset());
        item.setEnableClaimWorkflow(dto.getEnableClaimWorkflow());
        Long itemId = pickedItemService.create(userId, item);
        return itemId != null ? Result.success(itemId) : Result.error(500, "发布失败");
    }

    //如果用户觉得自己需要ai生成描述，也可手动添加
    @PostMapping("/regenerateAiDesc/{id}")
    public Result<String> regenerateAiDesc(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        //先查询物品（用于生成AI描述，同时也可校验是否存在）
        PickedItem item = pickedItemService.getById(id);
        if (item == null) {
            return Result.error(404, "物品不存在");
        }

        String newDesc = aiService.generateItemDescription(item.getName(), item.getDescription());
        PickedItem updateData = new PickedItem();
        updateData.setId(id);
        updateData.setAiDescription(newDesc);

        boolean success = pickedItemService.update(userId, id, updateData);
        if (success) {
            return Result.success("重新生成成功", newDesc);
        } else {
            return Result.error(403, "无权操作或更新失败");
        }
    }

    //生成AI描述（发布前）
    @PostMapping("/generateAiDesc")
    public Result<String> generateAiDesc(@RequestParam String name, @RequestParam String description, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        String aiDesc = aiService.generateItemDescription(name, description);
        System.out.println("生成的 AI 描述：" + aiDesc);
        if (aiDesc != null && !aiDesc.isEmpty()) {
            return Result.success("生成成功", aiDesc);
        } else {
            return Result.error(500, "生成失败");
        }
    }

    @PutMapping("/update")
    public Result<String> update(@RequestParam Long id, @RequestBody PickedItemDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PickedItem updateData = new PickedItem();//service层更新方法需要一个实体
        updateData.setName(dto.getName());
        updateData.setLocation(dto.getLocation());
        updateData.setPickTime(dto.getPickTime());
        updateData.setDescription(dto.getDescription());
        updateData.setImageUrl(dto.getImageUrl());
        updateData.setContact(dto.getContact());
        updateData.setVisibilityPreset(dto.getVisibilityPreset());
        updateData.setEnableClaimWorkflow(dto.getEnableClaimWorkflow());
        if (dto.getStatus() != null) {
            updateData.setStatus(dto.getStatus());
        }
        boolean success = pickedItemService.update(userId, id, updateData);
        return success ? Result.success("修改成功") : Result.error(403, "无权修改或记录不存在");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = pickedItemService.delete(userId, id);
        return success ? Result.success("删除成功") : Result.error(403, "删除失败或无权删除");
    }

    @DeleteMapping("/admin/delete/{id}")
    public Result<String> adminDeletePicked(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        if (adminId == null) {
            return Result.error(401, "未登录");
        }
        boolean success = pickedItemService.adminDelete(adminId, id);
        return success ? Result.success("删除成功") : Result.error(403, "无权操作或删除失败");
    }

    @GetMapping("/detail/{id}")
    public Result<PickedItemVo> detail(@PathVariable Long id, HttpServletRequest request) {
        PickedItem item = pickedItemService.getById(id);
        if (item == null) {
            return Result.error(404, "记录不存在");
        }

        //获取当前用户信息（可能未登录）
        Long currentUserId = (Long) request.getAttribute("userId");
        Integer currentRole = (Integer) request.getAttribute("role");
        System.out.println("Detail request - currentUserId: " + currentUserId + ", currentRole: " + currentRole);
        
        boolean isOwner = currentUserId != null && currentUserId.equals(item.getUserId());
        boolean isAdmin = currentRole != null && currentRole == 1;
        System.out.println("Detail request - isOwner: " + isOwner + ", isAdmin: " + isAdmin);
        
        //转换为VO
        PickedItemVo vo = ConvertUtil.convert(item, PickedItemVo.class);

        //如果是发布者本人或管理员，直接返回完整信息
        if (isOwner || isAdmin) {
            System.out.println("Detail request - Returning full info for owner or admin");
            return Result.success(vo);
        }

        //非本人且非管理员，才根据 visibility_preset 过滤
        VisibilityPresetEnum presetEnum = VisibilityPresetEnum.fromCode(item.getVisibilityPreset());
        System.out.println("Detail request - Visibility preset: " + presetEnum);
        
        // 非管理员且非发布者，根据可见性设置返回不同信息
        if (presetEnum == VisibilityPresetEnum.OWNER_ONLY) {
            // 仅发布者可见，非发布者拒绝访问
            System.out.println("Detail request - Returning 403 for OWNER_ONLY");
            return Result.error(403, "该物品仅发布者可见");
        }
        
        switch (presetEnum) {
            case HIDE_CONTACT:
                vo.setContact(null);
                break;
            case REGISTERED_ONLY:
                if (currentUserId == null) {
                    vo.setContact(null);
                    vo.setDescription(null);
                }
                break;
            default: //PUBLIC
                break;
        }

        return Result.success(vo);
    }


    @GetMapping("/list")
    public Result<Page<PickedItemVo>> list(
            @RequestParam(required = false) String location, @RequestParam(required = false) String name, @RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime,//不一定都会传入，筛选的时候可以不传入则全选，也可只传入一个
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "false") boolean includeAllStatus) {
        Page<PickedItem> pageResult = pickedItemService.pageByCondition(location, name, startTime, endTime, sortBy, page, size, includeAllStatus);
        Page<PickedItemVo> voPage = ConvertUtil.convertPage(pageResult, PickedItemVo.class);
        return Result.success(voPage);
    }

    @PostMapping("/search")
    public Result<List<PickedItemVo>> searchByDescription(@RequestParam String description) {
        if (description == null || description.trim().isEmpty()) {
            return Result.error(400, "描述不能为空");
        }
        //不要关键词预筛选
        //直接获取所有未认领的，然后后面交给AI去选
        List<PickedItem> candidates = pickedItemService.getAvailableItems(100);
        if (candidates.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        //AI 精排
        List<PickedItem> sorted = aiService.searchBestMatches(description, candidates);
        //转换为 VO 返回
        List<PickedItemVo> voList = ConvertUtil.convertList(sorted, PickedItemVo.class);
        return Result.success(voList);
    }
}