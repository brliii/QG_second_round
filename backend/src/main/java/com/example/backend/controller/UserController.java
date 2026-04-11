package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.utils.ConvertUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user){
        boolean success=userService.register(user);
        if(success){
            return Result.success("注册成功");
        }else{
            return Result.error(400,"注册失败，用户名、邮箱或手机号已存在");
        }
    }

    @PostMapping("/registerAdmin")
    public Result<String> registerAdmin(@RequestBody User user, @RequestParam String secretKey) {
        boolean success = userService.registerAdmin(user, secretKey);
        if (success) {
            return Result.success("管理员注册成功");
        } else {
            return Result.error(400, "注册失败，密钥错误或用户名/邮箱/手机号已存在");
        }
    }

    @PostMapping("/login")
    public Result<String> login(@RequestParam String account, @RequestParam String password) {
        String token = userService.login(account, password);
        if (token != null) {
            //将token放入data返回
            return Result.success("登录成功", token);
        } else {
            return Result.error(401, "账号或密码错误，或账号被封禁");
        }
    }

    @GetMapping("/info")
    public Result<UserDto> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        if (user != null) {
            UserDto userDto= ConvertUtil.convert(user, UserDto.class);
            return Result.success(userDto);
        } else {
            return Result.error(404, "用户不存在");
        }
    }


}
