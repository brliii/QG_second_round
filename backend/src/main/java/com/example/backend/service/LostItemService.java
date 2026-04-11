package com.example.backend.service;

import com.example.backend.entity.LostItem;
import java.util.List;

public interface LostItemService {
    //创建失物
    boolean create(Long userId, LostItem lostItem);
    //更新失物
    boolean update(Long userId, Long id, LostItem updateData);
    //逻辑删除失物
    boolean delete(Long userId, Long id);
    //根据ID查询
    LostItem getById(Long id);
    //条件分页列表
    List<LostItem> listByCondition(String location, String name, String sortBy, int page, int size);
}