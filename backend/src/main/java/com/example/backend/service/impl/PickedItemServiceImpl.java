package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.PickedItem;
import com.example.backend.mapper.PickedItemMapper;
import com.example.backend.service.PickedItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.spi.CurrencyNameProvider;

@Service
public class PickedItemServiceImpl implements PickedItemService {
    @Autowired
    private PickedItemMapper pickedItemMapper;

    @Override
    public boolean create(Long userId, PickedItem pickedItem) {
        pickedItem.setUserId(userId);
        pickedItem.setStatus(0);
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
    public boolean adminDelete(Long id) {
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
    public List<PickedItem> listByCondition(String location, String name, String sortBy, int curPage, int size) {
        QueryWrapper<PickedItem> wrapper = new QueryWrapper<>();
        if (location != null && !location.isEmpty()) {
            wrapper.eq("location", location);//地点要准确查询
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);//物品名称可以靠关键字模糊查询
        }
        wrapper.eq("status", 0); //只查未认领的
        if ("pickTime".equals(sortBy)) {
            wrapper.orderByDesc("pick_time");//降序，最近的最先显示
        } else {
            wrapper.orderByDesc("create_time");
        }
        Page<PickedItem> pageObject = new Page<>(curPage, size);
        Page<PickedItem> result = pickedItemMapper.selectPage(pageObject, wrapper);
        return result.getRecords();
    }
}