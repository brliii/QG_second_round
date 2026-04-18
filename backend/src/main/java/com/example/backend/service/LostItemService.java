package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.LostItem;
import java.util.List;

public interface LostItemService {
    //创建失物
    Long create(Long userId, LostItem lostItem);
    //用户自己，更新失物
    boolean update(Long userId, Long id, LostItem updateData);
    //管理员更新失物（在审核置顶会用到）
    boolean adminUpdate(LostItem lostItem);
    //用户自己，逻辑删除失物
    boolean delete(Long userId, Long id);
    //管理员删除(在管理员controller调用，需要管理员ID进行权限校验)
    boolean adminDelete(Long adminId, Long id);
    //根据ID查询
    LostItem getById(Long id);
    //条件分页列表
    Page<LostItem> pageByCondition(String location, String name, String startTime, String endTime, String sortBy, int page, int size);
    //关键词搜索
    List<LostItem> searchByKeyword(String keyword);
    //判断失物发布者与当前用户是否同个
    boolean isOwner(Long userId, Long lostItemId);
}