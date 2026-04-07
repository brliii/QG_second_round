package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.PickedItem;
import com.example.backend.mapper.PickedItemMapper;
import com.example.backend.service.PickedItemService;
import org.springframework.stereotype.Service;

@Service
public class PickedItemServiceImpl extends ServiceImpl<PickedItemMapper, PickedItem> implements PickedItemService{
}
