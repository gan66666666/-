package com.pet.adopt.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.adopt.dto.LoginDTO;
import com.pet.adopt.dto.LoginVO;
import com.pet.adopt.dto.UpdatePasswordDTO;
import com.pet.adopt.entity.SysUser;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.mapper.SysUserMapper;
import com.pet.adopt.service.SysUserService;
import com.pet.adopt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 管理员账号服务实现 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new BusinessException("用户名或密码错误");
        }
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginDTO.getUsername()));
        if (user == null || !BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        LoginVO vo = new LoginVO();
        // 管理端令牌 role 固定为 admin，与用户端令牌互不通用
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), JwtUtil.ROLE_ADMIN));
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(JwtUtil.ROLE_ADMIN);
        return vo;
    }

    @Override
    public SysUser getCurrentUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 返回前抹掉密码，避免泄露
        user.setPassword(null);
        return user;
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO) {
        if (updatePasswordDTO == null
                || updatePasswordDTO.getOldPassword() == null
                || updatePasswordDTO.getNewPassword() == null) {
            throw new BusinessException("旧密码与新密码不能为空");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!BCrypt.checkpw(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw(updatePasswordDTO.getNewPassword(), BCrypt.gensalt(10)));
        sysUserMapper.updateById(update);
    }
}
