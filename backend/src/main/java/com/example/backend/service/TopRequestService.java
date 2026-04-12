package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.TopRequest;

import java.util.List;

public interface TopRequestService {
    //创建置顶申请
    boolean create(TopRequest request);
    //获取待处理置顶申请
    List<TopRequest> getPendingRequests();
    //处理申请
    boolean approve(Long requestId,Long adminId,Integer approveStatus);
    //根据id获取申请
    TopRequest getById(Long id);
}
