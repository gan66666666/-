package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录成功返回结果
 */
@Data
public class LoginVO implements Serializable {

    /** JWT 令牌 */
    private String token;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 角色，管理员登录固定为 admin */
    private String role;
}
