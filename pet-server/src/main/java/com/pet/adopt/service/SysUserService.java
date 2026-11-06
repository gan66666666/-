package com.pet.adopt.service;

import com.pet.adopt.dto.LoginDTO;
import com.pet.adopt.dto.LoginVO;
import com.pet.adopt.dto.UpdatePasswordDTO;
import com.pet.adopt.entity.SysUser;

/** 管理员账号服务 */
public interface SysUserService {

    /** 登录：校验用户名密码，成功返回令牌与用户信息 */
    LoginVO login(LoginDTO loginDTO);

    /** 根据用户ID查询当前登录用户（不含密码） */
    SysUser getCurrentUser(Long userId);

    /** 修改密码：校验旧密码后写入新密码 */
    void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO);
}
