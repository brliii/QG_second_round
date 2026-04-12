package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.User;

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
    boolean banUser(Long userId,Integer status);
}
