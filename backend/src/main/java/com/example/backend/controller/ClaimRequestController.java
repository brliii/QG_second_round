package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.ClaimEvidenceDto;
import com.example.backend.dto.ClaimRequestCreateDto;
import com.example.backend.dto.ClaimRequestProcessDto;
import com.example.backend.entity.ClaimRequest;
import com.example.backend.entity.PickedItem;
import com.example.backend.entity.User;
import com.example.backend.service.ClaimRequestService;
import com.example.backend.service.PickedItemService;
import com.example.backend.service.UserService;
import com.example.backend.utils.ConvertUtil;
import com.example.backend.vo.ClaimRequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/claim")
public class ClaimRequestController {
    @Autowired
    private ClaimRequestService claimRequestService;
    @Autowired
    private PickedItemService pickedItemService;
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody ClaimRequestCreateDto dto, HttpServletRequest request) {
        Long claimantId = (Long) request.getAttribute("userId");
        if (claimantId == null) {
            return Result.error(401, "未登录");
        }
        PickedItem item = pickedItemService.getById(dto.getPickedItemId());
        if (item == null || item.getStatus() != 0) {
            return Result.error(400, "物品不存在或已被认领");
        }
        if (item.getUserId().equals(claimantId)) {
            return Result.error(400, "不能认领自己发布的物品");
        }
        ClaimRequest requestEntity = new ClaimRequest();
        requestEntity.setPickedItemId(dto.getPickedItemId());
        requestEntity.setClaimantId(claimantId);
        requestEntity.setVerifyAnswer(dto.getVerifyAnswer());
        boolean success = claimRequestService.create(requestEntity);
        return success ? Result.success("申请已提交，等待发布者审核") : Result.error(500, "申请失败");
    }

    //拾取者查看自己发布的拾取物品的所有申请
    @GetMapping("/listByPicker")
    public Result<List<ClaimRequestVo>> listByPicker(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<PickedItem> items = pickedItemService.getByUserId(userId);
        List<ClaimRequestVo> voList = new ArrayList<>();
        for (PickedItem item : items) {
            List<ClaimRequest> requests = claimRequestService.getByPickedItemId(item.getId());
            for (ClaimRequest cr : requests) {//一个物品可能存在多个认领申请
                ClaimRequestVo vo = ConvertUtil.convert(cr, ClaimRequestVo.class);
                vo.setPickedItemName(item.getName());
                User claimant=userService.getCurrentUser(cr.getClaimantId());
                if (claimant!=null){
                    vo.setClaimantUsername(claimant.getUsername());
                }
                voList.add(vo);
            }
        }
        return Result.success(voList);
    }

    //拾取者处理申请
    @PutMapping("/process/{requestId}")
    public Result<String> process(@PathVariable Long requestId, @RequestBody ClaimRequestProcessDto dto, HttpServletRequest request) {
        Long pickerId = (Long) request.getAttribute("userId");
        if (pickerId == null) {
            return Result.error(401, "未登录");
        }
        boolean success = claimRequestService.process(requestId, dto.getStatus(), dto.getComment(), pickerId);
        return success?Result.success("处理成功"):Result.error(400, "处理失败：可能无权限、申请不存在或已处理");
    }

    //失主查看自己的申请记录（含取件码）
    @GetMapping("/myRequests")
    public Result<List<ClaimRequestVo>> myRequests(HttpServletRequest request) {
        Long claimantId = (Long) request.getAttribute("userId");
        if (claimantId == null) {
            return Result.error(401, "未登录");
        }
        List<ClaimRequest> list = claimRequestService.getByClaimantId(claimantId);
        List<ClaimRequestVo> voList=new ArrayList<>();
        for (ClaimRequest cr : list) {
            ClaimRequestVo vo = ConvertUtil.convert(cr, ClaimRequestVo.class);
            PickedItem item = pickedItemService.getById(cr.getPickedItemId());
            if (item != null){
                vo.setPickedItemName(item.getName());
            }
            User claimant = userService.getCurrentUser(cr.getClaimantId());
            if (claimant != null){
                vo.setClaimantUsername(claimant.getUsername());
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    //失主补充证据
    @PutMapping("/evidence")
    public Result<String> submitEvidence(@RequestBody ClaimEvidenceDto dto, HttpServletRequest request) {
        Long claimantId = (Long) request.getAttribute("userId");
        if (claimantId == null) {
            return Result.error(401, "未登录");
        }
        boolean success = claimRequestService.submitEvidence(dto.getRequestId(), dto.getEvidence(), claimantId);
        return success ? Result.success("证据已提交") : Result.error(400, "提交失败：可能无权限或申请不存在");
    }
}