package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.UserService;
import com.example.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${admin.register-secret}")
    private String adminRegisterSecret;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();//密码加密，常量且不可修改

    @Override
    public boolean register(User user) {
        //检查
        //检查用户名、邮箱、手机号存在与否
        QueryWrapper<User> wrapperUsername = new QueryWrapper<>();
        wrapperUsername.eq("username",user.getUsername());
        if(userMapper.selectCount(wrapperUsername)>0){
            return false;
        }
        QueryWrapper<User> wrapperEmail = new QueryWrapper<>();
        wrapperEmail.eq("email", user.getEmail());
        if (userMapper.selectCount(wrapperEmail) > 0) {
            return false;
        }
        QueryWrapper<User> wrapperPhone = new QueryWrapper<>();
        wrapperPhone.eq("phone", user.getPhone());
        if (userMapper.selectCount(wrapperPhone) > 0) {
            return false;
        }

        //加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(0);
        user.setRole(0);
        user.setNickname(null);
        user.setAvatar(null);

        return userMapper.insert(user)>0;
    }


    @Override
    public boolean registerAdmin(User user, String secretKey) {
        if(!adminRegisterSecret.equals(secretKey)){
            return false;
        }

        QueryWrapper<User> wrapperUsername = new QueryWrapper<>();
        wrapperUsername.eq("username",user.getUsername());
        if(userMapper.selectCount(wrapperUsername)>0){
            return false;
        }
        QueryWrapper<User> wrapperEmail = new QueryWrapper<>();
        wrapperEmail.eq("email", user.getEmail());
        if (userMapper.selectCount(wrapperEmail) > 0) {
            return false;
        }
        QueryWrapper<User> wrapperPhone = new QueryWrapper<>();
        wrapperPhone.eq("phone", user.getPhone());
        if (userMapper.selectCount(wrapperPhone) > 0) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(0);
        user.setRole(1);
        user.setNickname(user.getNickname()==null?"管理员":user.getNickname());
        user.setAvatar(null);

        return userMapper.insert(user)>0;
    }


    @Override
    public String login(String account,String password) {
        //根据邮箱或手机号查询
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email",account).or().eq("phone",account);
        User user=userMapper.selectOne(wrapper);
        if(user==null){
            return null;
        }
        // 检查用户是否被封禁
        if (user.getStatus() == 1) {
            return null;
        }
        if(!passwordEncoder.matches(password,user.getPassword())){
            return null;
        }

        return jwtUtil.generateToken(user.getId(),user.getUsername(),user.getRole());
    }


    @Override
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public boolean updateUserStatus(User user) {
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean banUser(Long operatorId, Long userId, Integer status){
        //校验操作者是否为管理员
        User operator = userMapper.selectById(operatorId);
        if (operator == null || operator.getRole() != 1) {
            return false;
        }
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        //验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        //新密码加密
        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateUserInfo(Long userId, String nickname, String avatar, String phone) {
        User user = new User();
        user.setId(userId);
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        //如果所有字段都为 null，updateById 仍然会执行，但不会更新任何字段
        return userMapper.updateById(user) > 0;
    }

    @Override
    public List<User> getUserList(Long adminId) {
        // 验证操作者是否为管理员
        User admin = userMapper.selectById(adminId);
        if (admin == null || admin.getRole() != 1) {
            return new ArrayList<>();
        }
        // 返回所有用户列表
        return userMapper.selectList(null);
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
}