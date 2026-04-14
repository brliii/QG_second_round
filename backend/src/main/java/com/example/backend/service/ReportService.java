package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.Report;

import java.util.List;

public interface ReportService {
    //创建举报
    boolean create(Report report);
    //获取待处理的举报（需管理员权限）
    List<Report> getPendingReports(Long adminId);
    //处理举报（需管理员权限）
    boolean handleReport(Long reportId, Integer status, Long adminId);
    //根据id获取举报
    Report getById(Long id);
}