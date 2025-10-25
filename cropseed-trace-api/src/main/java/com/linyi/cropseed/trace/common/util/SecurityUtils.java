package com.linyi.cropseed.trace.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全工具类
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public class SecurityUtils {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取Authentication对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 加密密码
     */
    public static String encryptPassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }

    /**
     * 验证密码
     */
    public static boolean matchPassword(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 判断是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
