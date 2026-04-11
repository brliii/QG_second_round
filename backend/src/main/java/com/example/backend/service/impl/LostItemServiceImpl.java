package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.LostItem;
import com.example.backend.mapper.LostItemMapper;
import com.example.backend.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LostItemServiceImpl implements LostItemService {
    @Autowired
    private LostItemMapper lostItemMapper;

    @Override
    public boolean create(Long userId, LostItem lostItem) {
        lostItem.setUserId(userId);
        lostItem.setStatus(0);
        lostItem.setIsTop(0);
        return lostItemMapper.insert(lostItem) > 0;
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
        //status和isTop等字段不能由用户修改
        return lostItemMapper.updateById(existing) > 0;
    }

    @Override
    public boolean delete(Long userId, Long id) {
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        LostItem update = new LostItem();
        update.setStatus(2); //逻辑删除，避免误删导致数据丢失
        return lostItemMapper.update(update, wrapper) > 0;
    }

    @Override
    public LostItem getById(Long id) {
        return lostItemMapper.selectById(id);
    }

    @Override
    public List<LostItem> listByCondition(String location, String name, String sortBy, int curPage, int size) {
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        if (location != null && !location.isEmpty()) {
            wrapper.eq("location", location);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }//wrapper可叠加
        wrapper.eq("status", 0);//只筛选正常状态的
        if ("lostTime".equals(sortBy)) {
            wrapper.orderByDesc("lost_time");
        } else {
            wrapper.orderByDesc("create_time");
        }
        Page<LostItem> pageObject = new Page<>(curPage, size);
        Page<LostItem> result = lostItemMapper.selectPage(pageObject, wrapper);
        return result.getRecords();
    }
}