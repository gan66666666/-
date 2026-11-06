package com.pet.adopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 普通用户实体，对应 user_info 表
 * 与管理员账号 SysUser（sys_user 表）是两套独立体系，互不影响
 */
@Data
@TableName("user_info")
public class UserInfo implements Serializable {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    @TableField("username")
    private String username;

    /** 登录密码（BCrypt 加密存储） */
    @TableField("password")
    private String password;

    /** 昵称 */
    @TableField("nickname")
    private String nickname;

    /** 联系电话 */
    @TableField("phone")
    private String phone;

    /** 联系地址 */
    @TableField("address")
    private String address;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}

