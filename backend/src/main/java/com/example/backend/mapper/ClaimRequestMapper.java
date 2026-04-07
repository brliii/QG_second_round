package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ClaimRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClaimRequestMapper extends BaseMapper<ClaimRequest> {
}