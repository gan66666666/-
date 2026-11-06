package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 普通用户登录成功返回结果
 */
@Data
public class UserLoginVO implements Serializable {

    /** JWT 令牌，载荷中 role 固定为 user */
    private String token;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 角色，固定为 user */
    private String role;
}

