package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Report;
import com.example.backend.mapper.ReportMapper;
import com.example.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    @Autowired
    private ReportMapper reportMapper;

    @Override
    public boolean create(Report report) {
        report.setStatus(0); //刚创建就待审核
        return reportMapper.insert(report) > 0;
    }

    @Override
    public List<Report> getPendingReports() {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).orderByDesc("create_time");
        return reportMapper.selectList(wrapper);
    }

    @Override
    public boolean handleReport(Long reportId, Integer status, Long adminId) {
        //status: 1-驳回, 2-删除内容, 3-封禁用户
        Report report = reportMapper.selectById(reportId);
        if (report == null) return false;
        report.setStatus(status);
        return reportMapper.updateById(report) > 0;
        //注意：实际处理内容（删除失物/封禁用户）需要在调用此方法前由 Controller 执行
    }

    @Override
    public Report getById(Long id){
        return reportMapper.selectById(id);
    }
}
