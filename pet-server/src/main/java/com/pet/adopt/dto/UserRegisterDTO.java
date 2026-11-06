package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 普通用户注册入参
 */
@Data
public class UserRegisterDTO implements Serializable {

    /** 用户名 */
    private String username;

    /** 明文密码 */
    private String password;

    /** 昵称，为空时默认取用户名 */
    private String nickname;

    /** 联系电话 */
    private String phone;
}

