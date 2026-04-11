package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.PickedItemDto;
import com.example.backend.entity.PickedItem;
import com.example.backend.service.PickedItemService;
import com.example.backend.utils.ConvertUtil;
import com.example.backend.vo.PickedItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/picked")
public class PickedItemController {
    @Autowired
    private PickedItemService pickedItemService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody PickedItemDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PickedItem item = new PickedItem();//service层创建方法需要一个实体
        //这里存在字段名不一致，所以不能直接用转换工具
        item.setName(dto.getName());
        item.setLocation(dto.getLocation());
        item.setPickTime(dto.getPickTime());
        item.setDescription(dto.getDescription());
        item.setImageUrl(dto.getImageUrl());
        item.setContact(dto.getContact());
        item.setVisibilityPreset(dto.getVisibilityPreset());
        item.setEnableClaimWorkflow(dto.getEnableClaimWorkflow());
        boolean success = pickedItemService.create(userId, item);
        return success ? Result.success("发布成功") : Result.error(500, "发布失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestParam Long id, @RequestBody PickedItemDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PickedItem updateData = new PickedItem();//service层更新方法需要一个实体
        updateData.setName(dto.getName());
        updateData.setLocation(dto.getLocation());
        updateData.setPickTime(dto.getPickTime());
        updateData.setDescription(dto.getDescription());
        updateData.setImageUrl(dto.getImageUrl());
        updateData.setContact(dto.getContact());
        updateData.setVisibilityPreset(dto.getVisibilityPreset());
        updateData.setEnableClaimWorkflow(dto.getEnableClaimWorkflow());
        boolean success = pickedItemService.update(userId, id, updateData);
        return success ? Result.success("修改成功") : Result.error(403, "无权修改或记录不存在");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = pickedItemService.delete(userId, id);
        return success ? Result.success("删除成功") : Result.error(403, "删除失败或无权删除");
    }

    @GetMapping("/detail/{id}")
    public Result<PickedItemVo> detail(@PathVariable Long id, HttpServletRequest request) {
        PickedItem item = pickedItemService.getById(id);
        if (item == null) {
            return Result.error(404, "记录不存在");
        }

        //获取当前用户信息（可能未登录）
        Long currentUserId = (Long) request.getAttribute("userId");
        Integer currentRole = (Integer) request.getAttribute("role");
        boolean isOwner = currentUserId != null && currentUserId.equals(item.getUserId());
        boolean isAdmin = currentRole != null && currentRole == 1;
        //转换为VO
        PickedItemVo vo = ConvertUtil.convert(item, PickedItemVo.class);

        //如果是发布者本人或管理员，直接返回完整信息
        if (isOwner || isAdmin) {
            return Result.success(vo);
        }

        //非本人且非管理员，才根据 visibility_preset 过滤
        Integer preset = item.getVisibilityPreset();
        if (preset == null) preset = 0;

        switch (preset) {
            case 1: //隐藏联系方式
                vo.setContact(null);
                break;
            case 2: //仅注册用户可见全部
                if (currentUserId == null) {
                    vo.setContact(null);
                    vo.setDescription(null);
                }
                break;
            case 3: //仅自己可见（非本人不可见）
                return Result.error(403, "该物品仅发布者可见");
            default: //0 全公开
                break;
        }

        return Result.success(vo);
    }


    @GetMapping("/list")
    public Result<List<PickedItemVo>> list(@RequestParam(required = false) String location, @RequestParam(required = false) String name,//不一定都会传入，筛选的时候可以不传入则全选，也可只传入一个
                                           @RequestParam(defaultValue = "createTime") String sortBy, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        List<PickedItem> list = pickedItemService.listByCondition(location, name, sortBy, page, size);
        List<PickedItemVo> voList = ConvertUtil.convertList(list, PickedItemVo.class);
        return Result.success(voList);
    }
}