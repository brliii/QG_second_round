package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.LostItem;
import com.example.backend.mapper.LostItemMapper;
import com.example.backend.service.LostItemService;
import org.springframework.stereotype.Service;

@Service
public class LostItemServiceImpl extends ServiceImpl<LostItemMapper, LostItem> implements LostItemService {
}