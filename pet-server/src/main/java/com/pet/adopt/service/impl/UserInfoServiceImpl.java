package com.pet.adopt.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.adopt.dto.LoginDTO;
import com.pet.adopt.dto.UpdatePasswordDTO;
import com.pet.adopt.dto.UserInfoVO;
import com.pet.adopt.dto.UserLoginVO;
import com.pet.adopt.dto.UserProfileDTO;
import com.pet.adopt.dto.UserRegisterDTO;
import com.pet.adopt.entity.UserInfo;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.mapper.UserInfoMapper;
import com.pet.adopt.service.UserInfoService;
import com.pet.adopt.util.JwtUtil;
import com.pet.adopt.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 普通用户账号服务实现
 */
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final UserInfoMapper userInfoMapper;

    private final JwtUtil jwtUtil;

    @Override
    public Long register(UserRegisterDTO registerDTO) {
        if (registerDTO == null
                || !StringUtils.hasText(registerDTO.getUsername())
                || !StringUtils.hasText(registerDTO.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        String username = registerDTO.getUsername().trim();
        // 与 user_info 表的列宽一致
        ValidationUtil.maxLength(username, 50, "用户名");
        ValidationUtil.maxLength(registerDTO.getNickname(), 50, "昵称");
        ValidationUtil.maxLength(registerDTO.getPhone(), 20, "手机号");
        // BCrypt 只取前 72 字节，超长密码容易被静默截断，这里直接拦下
        ValidationUtil.maxLength(registerDTO.getPassword(), 32, "密码");

        // 先查重给出友好提示，唯一索引兜底防止并发注册绕过检查
        Long existCount = userInfoMapper.selectCount(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUsername, username));
        if (existCount != null && existCount > 0) {
            throw new BusinessException("用户名已存在");
        }

        UserInfo user = new UserInfo();
        user.setUsername(username);
        // 密码使用 BCrypt 加密后存储
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt(10)));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname())
                ? registerDTO.getNickname().trim() : username);
        user.setPhone(StringUtils.hasText(registerDTO.getPhone())
                ? registerDTO.getPhone().trim() : null);
        user.setCreateTime(LocalDateTime.now());
        try {
            userInfoMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名已存在");
        }
        return user.getId();
    }

    @Override
    public UserLoginVO login(LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new BusinessException("用户名或密码错误");
        }
        UserInfo user = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUsername, loginDTO.getUsername().trim()));
        if (user == null || !BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        UserLoginVO vo = new UserLoginVO();
        // 用户端令牌 role 固定为 user，与管理端 admin 令牌互不通用
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), JwtUtil.ROLE_USER));
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(JwtUtil.ROLE_USER);
        return vo;
    }

    @Override
    public UserInfoVO getCurrentUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO) {
        if (updatePasswordDTO == null
                || !StringUtils.hasText(updatePasswordDTO.getOldPassword())
                || !StringUtils.hasText(updatePasswordDTO.getNewPassword())) {
            throw new BusinessException("旧密码与新密码不能为空");
        }
        ValidationUtil.maxLength(updatePasswordDTO.getNewPassword(), 32, "新密码");
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!BCrypt.checkpw(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        UserInfo update = new UserInfo();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw(updatePasswordDTO.getNewPassword(), BCrypt.gensalt(10)));
        userInfoMapper.updateById(update);
    }

    @Override
    public void updateProfile(Long userId, UserProfileDTO profileDTO) {
        if (profileDTO == null) {
            throw new BusinessException("修改内容不能为空");
        }
        ValidationUtil.maxLength(profileDTO.getNickname(), 50, "昵称");
        ValidationUtil.maxLength(profileDTO.getPhone(), 20, "手机号");
        ValidationUtil.maxLength(profileDTO.getAddress(), 255, "联系地址");
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfo update = new UserInfo();
        update.setId(userId);
        // 只更新入参中非空的字段，未传的项保持原值
        if (StringUtils.hasText(profileDTO.getNickname())) {
            update.setNickname(profileDTO.getNickname().trim());
        }
        if (StringUtils.hasText(profileDTO.getPhone())) {
            update.setPhone(profileDTO.getPhone().trim());
        }
        if (StringUtils.hasText(profileDTO.getAddress())) {
            update.setAddress(profileDTO.getAddress().trim());
        }
        userInfoMapper.updateById(update);
    }

    /**
 * 实体转 VO，密码一律不带出
     */
    private UserInfoVO toVO(UserInfo user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAddress(user.getAddress());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}

