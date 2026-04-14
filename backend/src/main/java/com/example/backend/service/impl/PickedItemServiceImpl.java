package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public boolean create(Long userId, PickedItem pickedItem) {
        pickedItem.setUserId(userId);
        pickedItem.setStatus(0);

        //如果描述短于十个字，则调用ai生成补充描述
        String description = pickedItem.getDescription();
        if(description==null || description.length()<10){
            String aiDescription=aiService.generateItemDescription(pickedItem.getName(),description);
            pickedItem.setDescription(aiDescription);
        }
        return pickedItemMapper.insert(pickedItem) > 0;
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
        //status等字段不能由用户修改
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
    public Page<PickedItem> pageByCondition(String location, String name, String startTime, String endTime, String sortBy, int page, int size) {
        Page<PickedItem> pageObj = new Page<>(page, size);
        QueryWrapper<PickedItem> wrapper = new QueryWrapper<>();
        if (location != null && !location.isEmpty()) {
            wrapper.eq("location", location);
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
        wrapper.eq("status", 0);
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
}