package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.ClaimRequest;
import com.example.backend.entity.PickedItem;
import com.example.backend.mapper.ClaimRequestMapper;
import com.example.backend.mapper.PickedItemMapper;
import com.example.backend.service.ClaimRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class ClaimRequestServiceImpl extends ServiceImpl<ClaimRequestMapper,ClaimRequest> implements ClaimRequestService {
    @Autowired
    private ClaimRequestMapper claimRequestMapper;
    @Autowired
    private PickedItemMapper pickedItemMapper;

    @Override
    public boolean create(ClaimRequest request){
        request.setStatus(0);//待审核
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        return claimRequestMapper.insert(request)>0;
    }

    @Override
    public List<ClaimRequest> getByPickedItemId(Long pickedItemId){
        QueryWrapper<ClaimRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("picked_item_id",pickedItemId).orderByDesc("create_time");
        return claimRequestMapper.selectList(wrapper);
    }

    @Override
    public List<ClaimRequest> getByClaimantId(Long claimantId){
        QueryWrapper<ClaimRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("claimant_id",claimantId).orderByDesc("create_time");
        return claimRequestMapper.selectList(wrapper);
    }

    @Override
    public ClaimRequest getById(Long id){
        return claimRequestMapper.selectById(id);
    }

    @Override
    public boolean process(Long requestId, Integer status, String comment, Long pickerId) {
        ClaimRequest request = claimRequestMapper.selectById(requestId);
        if(request==null){
            return false;
        }
        //校验当前用户是否为拾取的发布者
        PickedItem item= pickedItemMapper.selectById(request.getPickedItemId());
        if(item==null || !item.getUserId().equals(pickerId)){
            return false;
        }
        //检查申请状态
        if(request.getStatus()!=0 && request.getStatus()!=4){
            return false;
        }

        request.setStatus(status);
        request.setUpdateTime(LocalDateTime.now());
        request.setPickerComment(comment);
        if(status==1){//同意认领，生产取件码
            String code=generatePickupCode();
            request.setPickupCode(code);
            request.setExpireTime(LocalDateTime.now().plusHours(24));
        }

        return claimRequestMapper.updateById(request)>0;
    }

    @Override
    public boolean submitEvidence(Long requestId, String evidence, Long claimantId) {
        ClaimRequest request = claimRequestMapper.selectById(requestId);
        if(request==null){
            return false;
        }
        //校验当前用户是否为申请人
        if(!request.getClaimantId().equals(claimantId)){
            return false;
        }
        //检查申请状态必须为要求补充证据
        if(request.getStatus()!=3){
            return false;
        }
        request.setEvidence(evidence);
        request.setStatus(4);//已补充证据
        request.setUpdateTime(LocalDateTime.now());
        return claimRequestMapper.updateById(request)>0;
    }

    @Override
    public String generatePickupCode(){
        Random random = new Random();
        int code=100000+random.nextInt(900000);
        return String.valueOf(code);
    }



}
