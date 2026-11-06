package com.pet.adopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体，对应 sys_user 表
 */
@Data
@TableName("sys_user")
public class SysUser {

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

    /** 头像地址 */
    @TableField("avatar")
    private String avatar;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}
