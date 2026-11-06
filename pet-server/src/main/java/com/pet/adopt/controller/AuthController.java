package com.pet.adopt.controller;

import com.pet.adopt.config.AuthInterceptor;
import com.pet.adopt.dto.LoginDTO;
import com.pet.adopt.dto.LoginVO;
import com.pet.adopt.dto.UpdatePasswordDTO;
import com.pet.adopt.entity.SysUser;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.service.SysUserService;
import com.pet.adopt.util.JwtUtil;
import com.pet.adopt.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 认证接口 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    private final JwtUtil jwtUtil;

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功", sysUserService.login(loginDTO));
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/info")
    public Result<SysUser> info(HttpServletRequest request) {
        return Result.success(sysUserService.getCurrentUser(getUserId(request)));
    }

    /** 退出登录：后端不做状态维护，由前端清除 token */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
    }

    /** 修改密码 */
    @PostMapping("/updatePassword")
    public Result<Void> updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO, HttpServletRequest request) {
        sysUserService.updatePassword(getUserId(request), updatePasswordDTO);
        return Result.success("密码修改成功", null);
    }

    /** 优先取拦截器写入的用户ID，取不到则回退到解析令牌 */
    private Long getUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(AuthInterceptor.USER_ID_ATTRIBUTE);
        if (attr instanceof Long) {
            return (Long) attr;
        }
        String header = request.getHeader("Authorization");
        if (header == null || header.isEmpty()) {
            throw new BusinessException("未登录或登录已过期");
        }
        String token = header.startsWith("Bearer ") ? header.substring(7).trim() : header.trim();
        return jwtUtil.getUserId(token);
    }
}
