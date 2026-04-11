package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String nickname;//昵称
    private String avatar;//头像url
    private Integer status;//0-正常 1封禁
    private Integer role;//0-用户 1-管理员
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
