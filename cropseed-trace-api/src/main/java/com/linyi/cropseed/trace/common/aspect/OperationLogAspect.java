package com.linyi.cropseed.trace.common.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.linyi.cropseed.trace.common.annotation.OperationLog;
import com.linyi.cropseed.trace.common.util.IpUtil;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.module.system.model.entity.SysOperationLog;
import com.linyi.cropseed.trace.module.system.service.SysOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 *
 * @author LinYi
 * @since 2025-11-28
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogService operationLogService;

    /**
     * 定义切点：所有带@OperationLog注解的方法
     */
    @Pointcut("@annotation(com.linyi.cropseed.trace.common.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取HttpServletRequest
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        
        // 构建日志对象
        SysOperationLog opLog = new SysOperationLog();
        opLog.setModule(operationLog.module());
        opLog.setContent(operationLog.content());
        
        // 设置当前用户信息
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            String username = SecurityUtils.getCurrentUsername();
            if (userId != null) {
                opLog.setUserId(userId);
            }
            if (StrUtil.isNotBlank(username)) {
                opLog.setUsername(username);
            }
        } catch (Exception e) {
            log.warn("获取当前用户信息失败", e);
        }
        
        // 设置请求信息
        if (request != null) {
            opLog.setUrl(request.getRequestURI());
            opLog.setMethod(request.getMethod());
            opLog.setIp(getIpAddress(request));
            opLog.setIpRegion(IpUtil.getRegion(opLog.getIp()));
            opLog.setUserAgent(request.getHeader("User-Agent"));
            
            // 记录请求参数（限制长度）
            String params = getRequestParams(joinPoint);
            if (StrUtil.isNotBlank(params)) {
                opLog.setParam(params.length() > 2000 ? params.substring(0, 2000) : params);
            }
        }
        
        // 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();
            opLog.setStatus(1); // 成功
        } catch (Exception e) {
            opLog.setStatus(0); // 失败
            String errorMsg = e.getMessage();
            if (StrUtil.isNotBlank(errorMsg)) {
                opLog.setErrorMessage(errorMsg.length() > 500 ? errorMsg.substring(0, 500) : errorMsg);
            }
            throw e;
        } finally {
            // 计算执行时间
            long executeTime = System.currentTimeMillis() - startTime;
            opLog.setExecuteTime(executeTime);
            opLog.setCreateTime(LocalDateTime.now());
            
            // 异步保存日志
            try {
                operationLogService.saveLog(opLog);
            } catch (Exception e) {
                log.error("保存操作日志异常", e);
            }
        }
        
        return result;
    }
    
    /**
     * 获取请求IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
    
    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            
            // 过滤掉HttpServletRequest、HttpServletResponse等对象
            StringBuilder params = new StringBuilder();
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                String className = arg.getClass().getName();
                if (className.startsWith("javax.servlet") || 
                    className.startsWith("jakarta.servlet") ||
                    className.startsWith("org.springframework")) {
                    continue;
                }
                
                try {
                    params.append(JSONUtil.toJsonStr(arg)).append(" ");
                } catch (Exception e) {
                    params.append(arg.toString()).append(" ");
                }
            }
            
            return params.toString().trim();
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
            return null;
        }
    }
}
