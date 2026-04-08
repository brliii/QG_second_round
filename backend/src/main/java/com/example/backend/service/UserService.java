package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.User;

public interface UserService {
    boolean register(User user);

    boolean registerAdmin(User user, String secretKey);

    String login(String account,String password);//需要返回个token所以string类型

    User getCurrentUser(Long userId);
}
