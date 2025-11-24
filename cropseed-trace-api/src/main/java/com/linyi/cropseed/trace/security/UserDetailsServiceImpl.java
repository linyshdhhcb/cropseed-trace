package com.linyi.cropseed.trace.security;

import com.linyi.cropseed.trace.common.constant.CommonConstant;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.module.system.model.entity.SysUser;
import com.linyi.cropseed.trace.module.wx.model.entity.WxUser;
import com.linyi.cropseed.trace.module.system.service.SysUserService;
import com.linyi.cropseed.trace.module.wx.service.WxUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 用户详情服务实现
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final String WX_USERNAME_PREFIX = "WX:";

    private final SysUserService sysUserService;
    private final WxUserService wxUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getUserByUsername(username);
        if (user != null) {
            List<GrantedAuthority> authorities = getUserAuthorities(user.getId());
            return User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(Objects.equals(user.getStatus(), CommonConstant.STATUS_DISABLE))
                    .credentialsExpired(false)
                    .disabled(Objects.equals(user.getStatus(), CommonConstant.STATUS_DISABLE))
                    .build();
        }

        // 微信小程序用户以 WX:{openid} 形式存储用户名
        if (username != null && username.startsWith(WX_USERNAME_PREFIX)) {
            String openid = username.substring(WX_USERNAME_PREFIX.length());
            WxUser wxUser = wxUserService.lambdaQuery()
                    .eq(WxUser::getOpenid, openid)
                    .one();
            if (wxUser == null) {
                throw new UsernameNotFoundException(ResultCode.USER_NOT_EXIST.getMessage());
            }

            boolean disabled = !Objects.equals(wxUser.getStatus(), CommonConstant.STATUS_ENABLE);
            return User.builder()
                    .username(username)
                    .password("")
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_WX_USER")))
                    .accountExpired(false)
                    .accountLocked(disabled)
                    .credentialsExpired(false)
                    .disabled(disabled)
                    .build();
        }

        throw new UsernameNotFoundException(ResultCode.USER_NOT_EXIST.getMessage());
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
