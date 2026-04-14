package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.common.Result;
import com.example.backend.entity.LostItem;
import com.example.backend.entity.PickedItem;
import com.example.backend.mapper.LostItemMapper;
import com.example.backend.mapper.PickedItemMapper;
import com.example.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private LostItemMapper lostItemMapper;
    @Autowired
    private PickedItemMapper pickedItemMapper;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(HttpServletRequest request) {
        //权限校验
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role != 1) {
            return Result.error(403, "仅管理员可访问");
        }

        Map<String, Object> stats = new HashMap<>();

        //总发布数量（失物+拾取）
        long lostCount = lostItemMapper.selectCount(null);
        long pickedCount = pickedItemMapper.selectCount(null);
        stats.put("totalPosts", lostCount + pickedCount);

        //找回物品数量（失物status=1已找回+拾取status=1已认领）
        long lostClaimed = lostItemMapper.selectCount(new QueryWrapper<LostItem>().eq("status", 1));
        long pickedClaimed = pickedItemMapper.selectCount(new QueryWrapper<PickedItem>().eq("status", 1));
        stats.put("totalClaimed", lostClaimed + pickedClaimed);

        //活跃用户数：近7天有发布行为的用户（去重）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        //近7天发布过失物的用户ID
        QueryWrapper<LostItem> lostWrapper=new QueryWrapper<>();
        lostWrapper.select("DISTINCT user_id").ge("create_time", sevenDaysAgo);//去重
        List<Object> lostUserIdsObj = lostItemMapper.selectObjs(lostWrapper);

        //近7天发布过拾取的用户ID
        QueryWrapper<PickedItem> pickedWrapper=new QueryWrapper<>();
        pickedWrapper.select("DISTINCT user_id").ge("create_time", sevenDaysAgo);//去重
        List<Object> pickedUserIdsObj = pickedItemMapper.selectObjs(pickedWrapper);

        //合并去重，set自带去重
        HashSet<Long> activeUserSet = new java.util.HashSet<>();
        for (Object obj : lostUserIdsObj) {
            if (obj != null) {
                activeUserSet.add(Long.valueOf(obj.toString()));
            }
        }
        for (Object obj : pickedUserIdsObj) {
            if (obj != null) {
                activeUserSet.add(Long.valueOf(obj.toString()));
            }
        }
        stats.put("activeUsers", activeUserSet.size());

        return Result.success(stats);
    }
}