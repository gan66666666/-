package com.pet.adopt.service;

import com.pet.adopt.dto.LoginDTO;
import com.pet.adopt.dto.UpdatePasswordDTO;
import com.pet.adopt.dto.UserInfoVO;
import com.pet.adopt.dto.UserLoginVO;
import com.pet.adopt.dto.UserProfileDTO;
import com.pet.adopt.dto.UserRegisterDTO;

/**
 * 普通用户账号服务（用户端）
 */
public interface UserInfoService {

    /**
     * 注册：用户名重复抛业务异常，成功返回新用户ID
     */
    Long register(UserRegisterDTO registerDTO);

    /**
     * 登录：校验用户名密码，成功返回令牌（role=user）与用户信息
     */
    UserLoginVO login(LoginDTO loginDTO);

    /**
     * 根据用户ID查询当前登录用户资料（不含密码）
     */
    UserInfoVO getCurrentUser(Long userId);

    /**
     * 修改密码：校验旧密码后写入新密码
     */
    void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO);

    /**
     * 修改资料：昵称、电话、地址，字段为空表示不修改该项
     */
    void updateProfile(Long userId, UserProfileDTO profileDTO);
}

