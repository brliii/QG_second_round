package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.ChangePasswordDto;
import com.example.backend.dto.UpdateUserInfoDto;
import com.example.backend.dto.UserDto;
import com.example.backend.dto.UserRegisterDto;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.utils.ConvertUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDto registerDto) {
        //校验两次密码是否一致
        if (registerDto.getPassword() == null || !registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            return Result.error(400, "两次输入的密码不一致");
        }
        //校验密码长度（6-20位）
        String pwd = registerDto.getPassword();
        if (pwd.length() < 6 || pwd.length() > 20) {
            return Result.error(400, "密码长度需在6-20位之间");
        }
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPhone(registerDto.getPhone());
        user.setPassword(registerDto.getPassword());
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.error(400, "用户名、邮箱或手机号已存在");
        }
    }

    @PostMapping("/registerAdmin")
    public Result<String> registerAdmin(@RequestBody UserRegisterDto registerDto, @RequestParam String secretKey) {
        //校验两次密码是否一致
        if (registerDto.getPassword() == null || !registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            return Result.error(400, "两次输入的密码不一致");
        }
        //校验密码长度（6-20位）
        String pwd = registerDto.getPassword();
        if (pwd.length() < 6 || pwd.length() > 20) {
            return Result.error(400, "密码长度需在6-20位之间");
        }
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPhone(registerDto.getPhone());
        user.setPassword(registerDto.getPassword());
        boolean success = userService.registerAdmin(user, secretKey);
        if (success) {
            return Result.success("管理员注册成功");
        } else {
            return Result.error(400, "注册失败：可能是密钥错误或用户名/邮箱/手机号已存在");
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

    @PutMapping("/ban/{userId}")
    public Result<String> banUser(@PathVariable Long userId, @RequestParam Integer status, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        if (operatorId == null) {
            return Result.error(401, "未登录");
        }
        boolean success = userService.banUser(operatorId, userId, status);
        return success ? Result.success("操作成功") : Result.error(403, "无权操作或用户不存在");
    }

    @PutMapping("/password")
    public Result<String> changePassword(@RequestBody ChangePasswordDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        //新密码长度校验
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6 || dto.getNewPassword().length() > 20) {
            return Result.error(400, "新密码长度需在6-20位之间");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            return Result.error(400, "两次输入的新密码不一致");
        }
        boolean success = userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return success?Result.success("密码修改成功"):Result.error(400, "旧密码错误或用户不存在");
    }

    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestBody UpdateUserInfoDto dto, HttpServletRequest request) {
        Long userId=(Long) request.getAttribute("userId");
        if (userId==null) {
            return Result.error(401, "未登录");
        }
        boolean success = userService.updateUserInfo(userId, dto.getNickname(), dto.getAvatar(), dto.getPhone());
        return success?Result.success("修改成功"):Result.error(500, "修改失败");
    }
}