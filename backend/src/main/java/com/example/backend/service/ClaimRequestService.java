package com.example.backend.service;

import com.example.backend.entity.ClaimRequest;
import java.util.List;

public interface ClaimRequestService {
    //发起认领申请
    boolean create(ClaimRequest request);
    //获取某个拾取物品的所有申请（拾取者查看）
    List<ClaimRequest> getByPickedItemId(Long pickedItemId);
    //获取某个失主的所有申请
    List<ClaimRequest> getByClaimantId(Long claimantId);
    //获取申请详情
    ClaimRequest getById(Long id);
    //处理申请（同意/拒绝/补充证据）
    boolean process(Long requestId, Integer status, String comment, Long pickerId);
    //生成取件码（同意时调用）
    String generatePickupCode();
}