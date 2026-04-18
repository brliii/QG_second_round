package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.PickedItem;
import java.util.List;

public interface PickedItemService {
    //创建拾取物品
    Long create(Long userId, PickedItem pickedItem);
    //更新拾取物品
    boolean update(Long userId, Long id, PickedItem updateData);
    //管理员更新拾取物品（在审核置顶会用到）
    boolean adminUpdate(PickedItem pickedItem);
    //用户，逻辑删除拾取物品
    boolean delete(Long userId, Long id);
    //管理员删除拾取物品（需要管理员ID进行权限校验）
    boolean adminDelete(Long adminId, Long id);
    //根据ID查询
    PickedItem getById(Long id);
    //条件分页列表
    Page<PickedItem> pageByCondition(String location, String name, String startTime, String endTime, String sortBy, int page, int size, boolean includeAllStatus);
    
    //检查并更新过期的置顶物品
    void checkAndUpdateExpiredTopItems();
    //根据id找
    List<PickedItem> getByUserId(Long userId);
    //根据关键词模糊查找用于AI排序前预筛选
    List<PickedItem> searchByKeywords(String keyword);
    //查找未认领的前n条数据
    List<PickedItem> getAvailableItems(int limit);
    //判断拾取物品发布者与当前用户是否同个
    boolean isOwner(Long userId, Long pickedItemId);
}