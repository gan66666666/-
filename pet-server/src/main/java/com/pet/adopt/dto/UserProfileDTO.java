package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 普通用户资料修改入参，字段为空表示不修改该项
 */
@Data
public class UserProfileDTO implements Serializable {

    /** 昵称 */
    private String nickname;

    /** 联系电话 */
    private String phone;

    /** 联系地址 */
    private String address;
}

