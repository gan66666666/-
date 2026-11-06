package com.pet.adopt.controller;

import com.pet.adopt.config.AuthInterceptor;
import com.pet.adopt.dto.LoginDTO;
import com.pet.adopt.dto.UpdatePasswordDTO;
import com.pet.adopt.dto.UserInfoVO;
import com.pet.adopt.dto.UserLoginVO;
import com.pet.adopt.dto.UserProfileDTO;
import com.pet.adopt.dto.UserQueryDTO;
import com.pet.adopt.dto.UserRegisterDTO;
import com.pet.adopt.entity.AdoptApply;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.service.AdoptApplyService;
import com.pet.adopt.service.UserInfoService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 普通用户账号接口（用户端）
 * 注意：server.servlet.context-path=/api，故对外完整路径为 /api/user/**
 * 权限：注册、登录公开；其余接口需要 role=user 的令牌
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserInfoService userInfoService;

    private final AdoptApplyService adoptApplyService;

    /**
     * 注册普通用户
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody UserRegisterDTO registerDTO) {
        return Result.success("注册成功", userInfoService.register(registerDTO));
    }

    /**
     * 普通用户登录，签发 role=user 的令牌
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功", userInfoService.login(loginDTO));
    }

    /**
     * 获取当前登录用户信息（不含密码）
     */
    @GetMapping("/info")
    public Result<UserInfoVO> info(HttpServletRequest request) {
        return Result.success(userInfoService.getCurrentUser(getUserId(request)));
    }

    /**
     * 修改密码：校验旧密码后写入新密码
     */
    @PutMapping("/updatePassword")
    public Result<Void> updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO,
                                       HttpServletRequest request) {
        userInfoService.updatePassword(getUserId(request), updatePasswordDTO);
        return Result.success("密码修改成功", null);
    }

    /**
     * 修改个人资料：昵称、电话、地址
     */
    @PutMapping("/profile")
    public Result<Void> profile(@RequestBody UserProfileDTO profileDTO, HttpServletRequest request) {
        userInfoService.updateProfile(getUserId(request), profileDTO);
        return Result.success("资料修改成功", null);
    }

    /**
     * 我的领养申请：按当前登录用户过滤，分页查询
     */
    @GetMapping("/myApply")
    public Result<PageResult<AdoptApply>> myApply(UserQueryDTO query, HttpServletRequest request) {
        return Result.success(adoptApplyService.pageMyApplies(getUserId(request), query));
    }

    /**
     * 取拦截器写入的当前用户ID，取不到说明令牌缺失，视为未登录
     */
    private Long getUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(AuthInterceptor.USER_ID_ATTRIBUTE);
        if (attr instanceof Long) {
            return (Long) attr;
        }
        throw new BusinessException(401, "未登录或登录已过期");
    }
}

