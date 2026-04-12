package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.LostItemDto;
import com.example.backend.entity.LostItem;
import com.example.backend.service.LostItemService;
import com.example.backend.utils.ConvertUtil;
import com.example.backend.vo.LostItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/lost")
public class LostItemController {

    @Autowired
    private LostItemService lostItemService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody LostItemDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LostItem item = new LostItem();//service层创建方法需要一个实体

        item.setName(dto.getName());
        item.setLocation(dto.getLocation());
        item.setLostTime(dto.getLostTime());
        item.setDescription(dto.getDescription());
        item.setImageUrl(dto.getImageUrl());
        item.setContact(dto.getContact());
        boolean success = lostItemService.create(userId, item);
        return success ? Result.success("发布成功") : Result.error(500, "发布失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestParam Long id, @RequestBody LostItemDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LostItem updateData = new LostItem();//service层更新方法需要一个实体
        updateData.setName(dto.getName());
        updateData.setLocation(dto.getLocation());
        updateData.setLostTime(dto.getLostTime());
        updateData.setDescription(dto.getDescription());
        updateData.setImageUrl(dto.getImageUrl());
        updateData.setContact(dto.getContact());
        boolean success = lostItemService.update(userId, id, updateData);
        return success ? Result.success("修改成功") : Result.error(403, "无权修改或记录不存在");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean success = lostItemService.delete(userId, id);
        return success ? Result.success("删除成功") : Result.error(403, "删除失败或无权删除");
    }

    @DeleteMapping("/admin/delete/{id}")
    public Result<String> adminDeleteLost(@PathVariable Long id, HttpServletRequest request) {
        Integer role=(Integer) request.getAttribute("role");
        if(role==null || role!=1){
            return Result.error(403,"仅管理员可操作");
        }
        boolean success=lostItemService.adminDelete(id);
        return success?Result.success("删除成功"):Result.error(500,"删除失败");
    }

    @GetMapping("/detail/{id}")
    public Result<LostItemVo> detail(@PathVariable Long id) {
        System.out.println("LostItemController.detail 被调用，id=" + id);
        LostItem item = lostItemService.getById(id);
        if (item == null) {
            return Result.error(404, "记录不存在");
        }
        LostItemVo vo = ConvertUtil.convert(item, LostItemVo.class);
        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<List<LostItemVo>> list(@RequestParam(required = false) String location, @RequestParam(required = false) String name,
                                         @RequestParam(defaultValue = "createTime") String sortBy, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        List<LostItem> list = lostItemService.listByCondition(location, name, sortBy, page, size);
        List<LostItemVo> voList = ConvertUtil.convertList(list, LostItemVo.class);
        return Result.success(voList);
    }
}