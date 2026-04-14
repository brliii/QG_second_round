package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.TopRequest;

import java.util.List;

public interface TopRequestService {
    //创建置顶申请
    boolean create(TopRequest request);
    //获取待处理置顶申请（需管理员权限）
    List<TopRequest> getPendingRequests(Long adminId);
    //处理申请（需管理员权限）
    boolean approve(Long requestId, Long adminId, Integer approveStatus);
    //根据id获取申请
    TopRequest getById(Long id);
}