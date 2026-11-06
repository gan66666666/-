package com.pet.adopt.config;

import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 登录拦截器：按「路径 + 角色」校验 Authorization 请求头中的 token
 *
 * 权限模型：
 * 1. 免登录接口 —— 公开接口，不需要 token；
 * 2. 用户端接口 —— 需要 role=user 的令牌；
 * 3. 管理端接口 —— 其余 /api/** 全部需要 role=admin 的令牌。
 * 令牌中的 role 由登录接口写入，用户端令牌与管理端令牌互不通用。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /** 请求属性中保存当前用户ID */
    public static final String USER_ID_ATTRIBUTE = "loginUserId";

    /** 请求属性中保存当前用户角色 */
    public static final String ROLE_ATTRIBUTE = "loginRole";

    /** 令牌请求头前缀 */
    private static final String TOKEN_PREFIX = "Bearer ";

    /** 未登录提示语 */
    private static final String UNAUTHORIZED_MSG = "未登录或登录已过期";

    /** 角色不符提示语 */
    private static final String FORBIDDEN_MSG = "无权限访问";

    /** 完全公开的接口（不需要 token），按「方法 + 路径」精确匹配 */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "POST /user/register",
            "POST /user/login",
            "POST /auth/login",
            "GET /pet/page",
            "GET /notice/page");

    /** 完全公开的详情接口（不需要 token）：/pet/{id}、/notice/{id} */
    private static final Set<String> PUBLIC_DETAIL_PREFIXES = Set.of("/pet", "/notice");

    /** 用户端专属前缀：/user/** 除注册登录外均需 role=user */
    private static final String USER_MODULE_PREFIX = "/user/";

    /** 用户端提交领养申请接口，需 role=user */
    private static final String USER_APPLY_PATH = "/apply";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // 放行跨域预检请求
            return true;
        }

        String method = request.getMethod().toUpperCase();
        String path = resolvePath(request);

        if (isPublic(method, path)) {
            return true;
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(401, UNAUTHORIZED_MSG);
        }

        Long userId;
        String role;
        try {
            userId = jwtUtil.getUserId(token);
            role = jwtUtil.getRole(token);
        } catch (BusinessException e) {
            // 令牌非法或已过期，统一按未登录处理
            throw new BusinessException(401, UNAUTHORIZED_MSG);
        }

        String requiredRole = requiredRole(method, path);
        if (!requiredRole.equals(role)) {
            // 角色不符：管理员令牌不能调用户端接口，用户端令牌也不能调管理端接口
            throw new BusinessException(403, FORBIDDEN_MSG);
        }

        request.setAttribute(USER_ID_ATTRIBUTE, userId);
        request.setAttribute(ROLE_ATTRIBUTE, role);
        return true;
    }

    /**
     * 判断接口是否完全公开（无需 token）
     */
    private boolean isPublic(String method, String path) {
        if (PUBLIC_PATHS.contains(method + " " + path)) {
            return true;
        }
        if (!"GET".equals(method)) {
            return false;
        }
        for (String prefix : PUBLIC_DETAIL_PREFIXES) {
            if (isDetailPath(path, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断接口需要的角色
     */
    private String requiredRole(String method, String path) {
        if (path.startsWith(USER_MODULE_PREFIX)) {
            return JwtUtil.ROLE_USER;
        }
        if ("POST".equals(method) && USER_APPLY_PATH.equals(path)) {
            return JwtUtil.ROLE_USER;
        }
        return JwtUtil.ROLE_ADMIN;
    }

    /**
     * 判断是否为公开的详情路径，形如 /pet/12、/notice/3
     */
    private boolean isDetailPath(String path, String prefix) {
        String head = prefix + "/";
        if (!path.startsWith(head)) {
            return false;
        }
        String segment = path.substring(head.length());
        if (segment.isEmpty() || segment.indexOf('/') >= 0) {
            return false;
        }
        try {
            Long.parseLong(segment);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 取出去掉上下文路径与末尾斜杠后的请求路径
     */
    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        if (uri.length() > 1 && uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri.isEmpty() ? "/" : uri;
    }

    /** 从请求头取出令牌，兼容 "Bearer xxx" 与裸 token 两种写法 */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header)) {
            return null;
        }
        if (header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length()).trim();
        }
        return header.trim();
    }
}

