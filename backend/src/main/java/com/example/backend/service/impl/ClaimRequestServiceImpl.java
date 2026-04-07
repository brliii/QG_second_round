package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.ClaimRequest;
import com.example.backend.mapper.ClaimRequestMapper;
import com.example.backend.service.ClaimRequestService;
import org.springframework.stereotype.Service;

@Service
public class ClaimRequestServiceImpl extends ServiceImpl<ClaimRequestMapper,ClaimRequest> implements ClaimRequestService {
}
