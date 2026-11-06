package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码请求参数
 */
@Data
public class UpdatePasswordDTO implements Serializable {

    /** 旧密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;
}
