package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求参数
 */
@Data
public class LoginDTO implements Serializable {

    /** 用户名 */
    private String username;

    /** 明文密码 */
    private String password;
}
