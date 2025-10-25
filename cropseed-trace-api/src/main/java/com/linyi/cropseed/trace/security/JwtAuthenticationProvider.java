package com.linyi.cropseed.trace.security;

import com.linyi.cropseed.trace.common.constant.CommonConstant;
import com.linyi.cropseed.trace.entity.SysUser;
import com.linyi.cropseed.trace.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证提供者
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        // 查询用户
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 检查用户状态
        if (!CommonConstant.STATUS_ENABLE.equals(user.getStatus())) {
            throw new BadCredentialsException("用户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 获取用户权限
        List<GrantedAuthority> authorities = getUserAuthorities(user.getId());

        return new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 获取用户权限
     */
    private List<GrantedAuthority> getUserAuthorities(Long userId) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 获取用户角色
        List<String> roles = sysUserService.getUserRoles(userId);
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        // 获取用户权限
        List<String> permissions = sysUserService.getUserPermissions(userId);
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        return authorities;
    }
}
