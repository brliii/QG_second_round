package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.Report;

import java.util.List;

public interface ReportService {
    //创建举报
    boolean create(Report report);
    //获取待处理的举报
    List<Report> getPendingReports();
    //处理举报
    boolean handleReport(Long reportId,Integer status,Long adminId);
}
