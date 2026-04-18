package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.User;

import java.util.List;

public interface UserService {
    //注册
    boolean register(User user);
    //管理员注册
    boolean registerAdmin(User user, String secretKey);
    //登录
    String login(String account,String password);//需要返回个token所以string类型
    //获取信息
    User getCurrentUser(Long userId);
    //更新用户状态（和举报有关）
    boolean updateUserStatus(User user);
    //封禁或解封用户 0-解封 1-封禁
    boolean banUser(Long operatorId,Long userId,Integer status);
    //修改密码
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    //更新用户信息
    boolean updateUserInfo(Long userId, String nickname, String avatar, String phone);
    //获取用户列表（管理员）
    List<User> getUserList(Long adminId);
}
