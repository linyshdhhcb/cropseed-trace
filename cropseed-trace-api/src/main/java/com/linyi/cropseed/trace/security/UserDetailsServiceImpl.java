package com.linyi.cropseed.trace.security;

import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.entity.SysUser;
import com.linyi.cropseed.trace.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(ResultCode.USER_NOT_EXIST.getMessage());
        }

        // 获取用户权限
        List<GrantedAuthority> authorities = getUserAuthorities(user.getId());

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!ResultCode.USER_LOCKED.getCode().equals(user.getStatus()))
                .credentialsExpired(false)
                .disabled(!ResultCode.USER_ACCOUNT_FORBIDDEN.getCode().equals(user.getStatus()))
                .build();
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
