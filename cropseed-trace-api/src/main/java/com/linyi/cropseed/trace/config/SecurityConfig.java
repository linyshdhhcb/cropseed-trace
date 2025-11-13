package com.linyi.cropseed.trace.config;

import com.linyi.cropseed.trace.security.JwtAuthenticationFilter;
import com.linyi.cropseed.trace.security.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new ProviderManager(jwtAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用CORS（使用自定义配置）
                .cors(AbstractHttpConfigurer::disable)
                // 会话管理
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 请求授权
                .authorizeHttpRequests(auth -> auth
                        // 公开接口（不需要登录）
                        .requestMatchers(
                                "/auth/**",
                                "/wx/auth/login",
                                "/wx/product/**", // 商品相关（分类、详情、列表等，公开访问）
                                "/wx/recommend/list", // 推荐列表（已支持未登录）
                                "/api/payment/*",//支付宝支付回调
                                "/file/upload",
                                "/wechat/**",
                                "/miniapp/**",
                                "/doc.html",
                                "/webjars/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/favicon.ico")
                        .permitAll()
                        // 小程序接口（需要登录，通过JWT过滤器验证，控制器中会检查登录状态）
                        // 注意：这些接口允许访问，但如果没有有效token，控制器会返回业务错误
                        .requestMatchers(
                                "/wx/order/**", // 订单相关
                                "/wx/cart/**", // 购物车相关
                                "/wx/address/**", // 地址相关
                                "/wx/after-sales/**", // 售后相关
                                "/wx/user/**", // 用户信息相关
                                "/wx/recommend/behavior", // 行为上报
                                "/wx/auth/logout") // 登出
                        .permitAll() // 改为 permitAll，让 JWT 过滤器处理认证，控制器检查登录状态
                        // 其他请求需要认证（管理后台接口）
                        .anyRequest().authenticated())
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
