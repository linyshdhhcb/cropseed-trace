package com.linyi.cropseed.trace.module.system.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.system.model.dto.LoginDTO;
import com.linyi.cropseed.trace.module.system.model.entity.SysUser;
import com.linyi.cropseed.trace.security.JwtTokenProvider;
import com.linyi.cropseed.trace.module.system.service.SysUserService;
import com.linyi.cropseed.trace.module.system.model.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Tag(name = "认证管理", description = "用户登录、登出、获取用户信息等接口")
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
    public Result<Void> logout(HttpServletRequest request) {
        try {
            // 从请求头中获取Token
            String token = getTokenFromRequest(request);
            if (token != null) {
                // 将Token加入黑名单
                jwtTokenProvider.addTokenToBlacklist(token);
                log.info("用户登出成功，Token已加入黑名单");
            }
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
            return Result.fail("登出失败");
        }
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
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
