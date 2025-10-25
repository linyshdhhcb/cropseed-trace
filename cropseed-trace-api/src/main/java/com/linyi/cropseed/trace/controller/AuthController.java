package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.LoginDTO;
import com.linyi.cropseed.trace.entity.SysUser;
import com.linyi.cropseed.trace.security.JwtTokenProvider;
import com.linyi.cropseed.trace.service.SysUserService;
import com.linyi.cropseed.trace.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "认证管理",description = "用户登录、登出、获取用户信息等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserService sysUserService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // 执行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        // 获取用户信息
        Long userId = (Long) authentication.getPrincipal();
        SysUser user = sysUserService.getById(userId);

        // 生成Token
        String token = jwtTokenProvider.generateToken(userId, user.getUsername());

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(userId);
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setAvatar(user.getAvatar());

        return Result.success("登录成功", loginVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // TODO: 实现登出逻辑（如将Token加入黑名单）
        return Result.success("登出成功");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public Result<LoginVO> getUserInfo() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        LoginVO loginVO = new LoginVO();

        // 从 Authentication 中获取用户信息
        Object principal = authentication.getPrincipal();
        if (principal instanceof SysUser) {
            SysUser user = (SysUser) principal;
            // 从用户对象中获取ID
            loginVO.setUserId(user.getId());
            loginVO.setUsername(user.getUsername());
            loginVO.setRealName(user.getRealName());
        } else {
            loginVO.setUsername(authentication.getName());
        }

        loginVO.setAvatar(authentication.getName());
        loginVO.setToken((String) authentication.getCredentials());

        return Result.success(loginVO);
    }

}
