package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 普通用户资料返回结果（不含密码）
 */
@Data
public class UserInfoVO implements Serializable {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 联系电话 */
    private String phone;

    /** 联系地址 */
    private String address;

    /** 创建时间 */
    private LocalDateTime createTime;
}

