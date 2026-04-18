package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.LostItem;
import com.example.backend.entity.User;
import com.example.backend.mapper.LostItemMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LostItemServiceImpl implements LostItemService {
    @Autowired
    private LostItemMapper lostItemMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Long create(Long userId, LostItem lostItem) {
        lostItem.setUserId(userId);
        lostItem.setStatus(0);
        lostItem.setIsTop(0);
        if (lostItemMapper.insert(lostItem) > 0) {
            return lostItem.getId();
        }
        return null;
    }

    @Override
    public boolean update(Long userId, Long id, LostItem updateData) {
        LostItem existing = lostItemMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {//一个是操作者id，一个是目标失物的人id
            return false;
        }
        existing.setName(updateData.getName());
        existing.setLocation(updateData.getLocation());
        existing.setLostTime(updateData.getLostTime());
        existing.setDescription(updateData.getDescription());
        existing.setImageUrl(updateData.getImageUrl());
        existing.setContact(updateData.getContact());
        // 允许用户修改状态（已找回/未找回）
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        return lostItemMapper.updateById(existing) > 0;
    }

    @Override
    public boolean adminUpdate(LostItem lostItem) {
        return lostItemMapper.updateById(lostItem) > 0;
    }

    @Override
    public boolean delete(Long userId, Long id) {//这个是用户自己删除自己的
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();//查询条件
        wrapper.eq("id", id).eq("user_id", userId);
        LostItem update = new LostItem();
        update.setStatus(2); //逻辑删除，避免误删导致数据丢失
        return lostItemMapper.update(update, wrapper) > 0;
    }

    @Override
    public boolean adminDelete(Long adminId, Long id) {
        User admin = userMapper.selectById(adminId);
        if (admin == null || admin.getRole() != 1) {
            return false;
        }
        LostItem lostItem = new LostItem();
        lostItem.setId(id);
        lostItem.setStatus(2); //逻辑删除
        return lostItemMapper.updateById(lostItem) > 0;
    }

    @Override
    public LostItem getById(Long id) {
        return lostItemMapper.selectById(id);
    }

    @Override
    public Page<LostItem> pageByCondition(String location, String name, String startTime, String endTime, String sortBy, int page, int size, boolean includeAllStatus) {
        // 先检查并更新过期的置顶物品
        checkAndUpdateExpiredTopItems();
        
        Page<LostItem> pageObj = new Page<>(page, size);
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        if (location != null && !location.isEmpty()) {
            wrapper.like("location", location);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge("lost_time", startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le("lost_time", endTime);
        }
        if (!includeAllStatus) {
            wrapper.eq("status", 0); //正常状态
        } else {
            wrapper.in("status", 0, 1); //包含正常和已找回状态，排除已删除
        }
        // 先按置顶状态排序，再按指定字段排序
        wrapper.orderByDesc("is_top");
        if ("lostTime".equals(sortBy)) {
            wrapper.orderByDesc("lost_time");
        } else {
            wrapper.orderByDesc("create_time");
        }
        return lostItemMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public boolean isOwner(Long userId, Long lostItemId) {
        LostItem item = lostItemMapper.selectById(lostItemId);
        return item != null && item.getUserId().equals(userId);//不存在也顺便返回了
    }
    
    @Override
    public List<LostItem> searchByKeyword(String keyword) {
        // 先检查并更新过期的置顶物品
        checkAndUpdateExpiredTopItems();
        
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0); // 只查询正常状态的失物
        // 如果有关键词，先尝试关键词筛选（用于AI预筛选，减少候选集）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like("name", keyword)
                    .or()
                    .like("description", keyword)
                    .or()
                    .like("location", keyword)
            );
        }
        // 先按置顶状态排序，再按创建时间排序
        wrapper.orderByDesc("is_top");
        wrapper.orderByDesc("create_time");
        wrapper.last("limit 50"); // 增加limit，确保有足够的候选物品供AI排序
        return lostItemMapper.selectList(wrapper);
    }
    
    @Override
    public void checkAndUpdateExpiredTopItems() {
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<LostItem> wrapper = new UpdateWrapper<>();
        wrapper.eq("is_top", 1)
               .lt("top_expire", now);
        
        LostItem update = new LostItem();
        update.setIsTop(0);
        update.setTopExpire(null);
        
        lostItemMapper.update(update, wrapper);
    }
}