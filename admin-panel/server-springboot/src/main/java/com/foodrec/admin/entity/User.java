package com.foodrec.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    private String avatar;
    private String openid;        // 微信 openid，用于小程序登录
    private Integer status;       // 1=正常, 0=禁用
    private LocalDateTime registerTime;
}

/**
 * 如果数据库 user 表还没有 openid 字段，执行以下 SQL：
 * ALTER TABLE user ADD COLUMN openid VARCHAR(100) NULL AFTER avatar;
 */
