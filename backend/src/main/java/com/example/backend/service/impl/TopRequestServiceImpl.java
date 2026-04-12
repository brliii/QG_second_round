package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.TopRequest;
import com.example.backend.mapper.TopRequestMapper;
import com.example.backend.service.TopRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TopRequestServiceImpl extends ServiceImpl<TopRequestMapper,TopRequest> implements TopRequestService {
    @Autowired
    private TopRequestMapper topRequestMapper;

    @Override
    public boolean create(TopRequest request) {
        request.setStatus(0);//刚创建就待审核
        request.setRequestTime(LocalDateTime.now());
        return topRequestMapper.insert(request)>0;
    }

    @Override
    public List<TopRequest> getPendingRequests() {
        QueryWrapper<TopRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("status",0).orderByDesc("request_time");
        return topRequestMapper.selectList(wrapper);
    }

    @Override
    public boolean approve(Long requestId,Long adminId,Integer approveStatus) {
        TopRequest request = topRequestMapper.selectById(requestId);
        if(request==null){
            return false;
        }
        request.setStatus(approveStatus);
        request.setAdminId(adminId);
        request.setApproveTime(LocalDateTime.now());
        return topRequestMapper.updateById(request)>0;
    }

    @Override
    public TopRequest getById(Long id) {
        return topRequestMapper.selectById(id);
    }
}
