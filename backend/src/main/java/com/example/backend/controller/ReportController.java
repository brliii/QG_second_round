package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.ReportDto;
import com.example.backend.entity.Report;
import com.example.backend.entity.User;
import com.example.backend.service.*;
import com.example.backend.utils.ConvertUtil;
import com.example.backend.vo.ReportVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {
    @Autowired
    private ReportService  reportService;
    @Autowired
    private UserService  userService;
    @Autowired
    private LostItemService  lostItemService;
    @Autowired
    private PickedItemService  pickedItemService;
    @Autowired
    private CommentService   commentService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody ReportDto dto, HttpServletRequest request){
        Long reporterId = (Long) request.getAttribute("userId");
        if(reporterId == null){
            return Result.error(401,"未登录");
        }

        Report report =new Report();
        report.setReporterId(reporterId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        boolean success=reportService.create(report);

        return success? Result.success("举报成功，等待审核"):Result.error(500,"举报失败");
    }

    @GetMapping("/pending")
    public Result<List<ReportVo>> getPendingReports(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        if (adminId == null) {
            return Result.error(401, "未登录");
        }
        List<Report> reports = reportService.getPendingReports(adminId);
        if (reports == null) {
            return Result.error(403, "仅管理员可访问");
        }
        List<ReportVo> voList = new ArrayList<>();
        for (Report r : reports) {
            ReportVo vo = ConvertUtil.convert(r, ReportVo.class);
            User reporter = userService.getCurrentUser(r.getReporterId());
            if (reporter != null) {
                vo.setReporterUsername(reporter.getUsername());
            }
            
            // 添加被举报对象信息
            if (r.getTargetType() != null && r.getTargetId() != null) {
                String targetInfo = null;
                if (r.getTargetType() == 0) {
                    // 失物
                    targetInfo = lostItemService.getById(r.getTargetId()).getName();
                } else if (r.getTargetType() == 1) {
                    // 拾取
                    targetInfo = pickedItemService.getById(r.getTargetId()).getName();
                } else if (r.getTargetType() == 2) {
                    // 评论
                    targetInfo = commentService.getById(r.getTargetId()).getContent();
                } else if (r.getTargetType() == 3) {
                    // 用户
                    User targetUser = userService.getCurrentUser(r.getTargetId());
                    if (targetUser != null) {
                        targetInfo = targetUser.getUsername();
                    }
                }
                vo.setTargetInfo(targetInfo);
            }
            
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @PutMapping("/handle/{reportId}")
    public Result<String> handleReport(@PathVariable("reportId") Long reportId, @RequestParam Integer status , HttpServletRequest request){
        Long adminId = (Long) request.getAttribute("userId");
        if (adminId == null) {
            return Result.error(401, "未登录");
        }

        Report report = reportService.getById(reportId);
        if(report == null){
            return Result.error(404,"举报不存在");
        }
        //根据目标status处理不同的举报
        if(status == 1){
            //不需要做任何操作，只需要在最后统一修改状态即可
        } else if(status == 2){//删除
            Integer targetType = report.getTargetType();
            Long targetId = report.getTargetId();
            if(targetType == 0){
                lostItemService.adminDelete(adminId, targetId);
            } else if(targetType == 1){
                pickedItemService.adminDelete(adminId, targetId);
            } else if(targetType == 2){//这块不一样，是物理删除，需要管理员ID
                commentService.deleteById(adminId, targetId);
            }
        } else if(status == 3){//封禁用户
            Long targetUserId = report.getTargetId();
            User user = userService.getCurrentUser(targetUserId);
            if(user != null){
                user.setStatus(1);
                userService.updateUserStatus(user);
            }
        }
        //最后统一更新举报状态
        boolean success = reportService.handleReport(reportId, status, adminId);
        return success ? Result.success("处理成功") : Result.error(500, "处理失败");
    }
}