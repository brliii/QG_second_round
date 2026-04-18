package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.PickedItem;
import java.util.List;

public interface PickedItemService {
    //创建拾取物品
    Long create(Long userId, PickedItem pickedItem);
    //更新拾取物品
    boolean update(Long userId, Long id, PickedItem updateData);
    //用户，逻辑删除拾取物品
    boolean delete(Long userId, Long id);
    //管理员删除拾取物品（需要管理员ID进行权限校验）
    boolean adminDelete(Long adminId, Long id);
    //根据ID查询
    PickedItem getById(Long id);
    //条件分页列表
    Page<PickedItem> pageByCondition(String location, String name, String startTime, String endTime, String sortBy, int page, int size);
    //根据id找
    List<PickedItem> getByUserId(Long userId);
    //根据关键词模糊查找用于AI排序前预筛选
    List<PickedItem> searchByKeywords(String keyword);
}