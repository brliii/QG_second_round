package com.example.backend.service;

import com.example.backend.entity.PickedItem;
import java.util.List;

public interface PickedItemService {
    //创建拾取物品
    boolean create(Long userId, PickedItem pickedItem);
    //更新拾取物品
    boolean update(Long userId, Long id, PickedItem updateData);
    //用户，逻辑删除拾取物品
    boolean delete(Long userId, Long id);
    //管理员删除拾取物品
    boolean adminDelete(Long id);
    //根据ID查询
    PickedItem getById(Long id);
    //条件分页列表
    List<PickedItem> listByCondition(String location, String name, String sortBy, int page, int size);
}