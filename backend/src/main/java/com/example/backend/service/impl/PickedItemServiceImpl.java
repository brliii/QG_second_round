package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.Result;
import com.example.backend.entity.PickedItem;
import com.example.backend.entity.User;
import com.example.backend.mapper.PickedItemMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.AiService;
import com.example.backend.service.PickedItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.spi.CurrencyNameProvider;

@Service
public class PickedItemServiceImpl implements PickedItemService {
    @Autowired
    private PickedItemMapper pickedItemMapper;
    @Autowired
    private AiService aiService;
    @Autowired
    private UserMapper userMapper;   // 新增，用于校验管理员身份

    @Override
    public Long create(Long userId, PickedItem pickedItem) {
        pickedItem.setUserId(userId);
        pickedItem.setStatus(0);

        //如果描述短于十个字，则调用ai生成补充描述
        String description = pickedItem.getDescription();
        if(description==null || description.length()<10){
            String aiDescription=aiService.generateItemDescription(pickedItem.getName(),description);
            pickedItem.setDescription(aiDescription);
        }
        if (pickedItemMapper.insert(pickedItem) > 0) {
            return pickedItem.getId();
        }
        return null;
    }

    @Override
    public boolean update(Long userId, Long id, PickedItem updateData) {
        PickedItem existing = pickedItemMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        existing.setName(updateData.getName());
        existing.setLocation(updateData.getLocation());
        existing.setPickTime(updateData.getPickTime());
        existing.setDescription(updateData.getDescription());
        existing.setImageUrl(updateData.getImageUrl());
        existing.setContact(updateData.getContact());
        existing.setVisibilityPreset(updateData.getVisibilityPreset());
        existing.setEnableClaimWorkflow(updateData.getEnableClaimWorkflow());
        // 允许用户修改状态（已认领/未认领）
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        return pickedItemMapper.updateById(existing) > 0;
    }

    @Override
    public boolean delete(Long userId, Long id) {
        QueryWrapper<PickedItem> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        PickedItem update = new PickedItem();
        update.setStatus(2); //逻辑删除
        return pickedItemMapper.update(update, wrapper) > 0;
    }

    @Override
    public boolean adminDelete(Long adminId, Long id) {
        User admin = userMapper.selectById(adminId);
        if (admin == null || admin.getRole() != 1) {
            return false;
        }
        PickedItem pickedItem = new PickedItem();
        pickedItem.setId(id);
        pickedItem.setStatus(2);//逻辑删除
        return pickedItemMapper.updateById(pickedItem) > 0;
    }

    @Override
    public PickedItem getById(Long id) {
        return pickedItemMapper.selectById(id);
    }

    @Override
    public Page<PickedItem> pageByCondition(String location, String name, String startTime, String endTime, String sortBy, int page, int size, boolean includeAllStatus) {
        // 先检查并更新过期的置顶物品
        checkAndUpdateExpiredTopItems();
        
        Page<PickedItem> pageObj = new Page<>(page, size);
        QueryWrapper<PickedItem> wrapper = new QueryWrapper<>();
        if (location != null && !location.isEmpty()) {
            wrapper.like("location", location);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge("pick_time", startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le("pick_time", endTime);
        }
        if (!includeAllStatus) {
            wrapper.eq("status", 0);
        } else {
            wrapper.in("status", 0, 1); //包含正常和已认领状态，排除已删除
        }
        // 先按置顶状态排序，再按指定字段排序
        wrapper.orderByDesc("is_top");
        if ("pickTime".equals(sortBy)) {
            wrapper.orderByDesc("pick_time");
        } else {
            wrapper.orderByDesc("create_time");
        }
        return pickedItemMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public List<PickedItem> getByUserId(Long userId) {
        QueryWrapper<PickedItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return pickedItemMapper.selectList(wrapper);
    }

    @Override
    public List<PickedItem> searchByKeywords(String keyword) {
        // 先检查并更新过期的置顶物品
        checkAndUpdateExpiredTopItems();
        
        QueryWrapper<PickedItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        // 如果有关键词，先尝试关键词筛选（用于AI预筛选，减少候选集）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like("name", keyword)
                    .or()
                    .like("description", keyword)
            );
        }
        // 先按置顶状态排序，再按创建时间排序
        wrapper.orderByDesc("is_top");
        wrapper.orderByDesc("create_time");
        wrapper.last("limit 50"); // 增加limit，确保有足够的候选物品供AI排序
        return pickedItemMapper.selectList(wrapper);
    }

    @Override
    public List<PickedItem> getAvailableItems(int limit) {
        // 先检查并更新过期的置顶物品
        checkAndUpdateExpiredTopItems();
        
        QueryWrapper<PickedItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0)
                // 先按置顶状态排序，再按创建时间排序
                .orderByDesc("is_top")
                .orderByDesc("create_time")
                .last("limit " + limit);
        return pickedItemMapper.selectList(wrapper);
    }

    @Override
    public boolean adminUpdate(PickedItem pickedItem) {
        return pickedItemMapper.updateById(pickedItem) > 0;
    }

    @Override
    public boolean isOwner(Long userId, Long pickedItemId) {
        PickedItem pickedItem = pickedItemMapper.selectById(pickedItemId);
        return pickedItem != null && pickedItem.getUserId().equals(userId);
    }
    
    @Override
    public void checkAndUpdateExpiredTopItems() {
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<PickedItem> wrapper = new UpdateWrapper<>();
        wrapper.eq("is_top", 1)
               .lt("top_expire", now);
        
        PickedItem update = new PickedItem();
        update.setIsTop(0);
        update.setTopExpire(null);
        
        pickedItemMapper.update(update, wrapper);
    }
}