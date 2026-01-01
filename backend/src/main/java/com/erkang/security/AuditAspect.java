package com.erkang.security;

import com.erkang.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    
    private final AuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        HttpServletRequest request = getRequest();
        String requestMethod = request != null ? request.getMethod() : null;
        String requestUrl = request != null ? request.getRequestURI() : null;
        String ipAddress = request != null ? getClientIp(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        
        String requestParams = null;
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                requestParams = objectMapper.writeValueAsString(args[0]);
            }
        } catch (Exception e) {
            log.debug("序列化请求参数失败: {}", e.getMessage());
        }
        
        Object result = null;
        Integer responseCode = 0;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            responseCode = -1;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            auditService.log(
                    auditable.action(),
                    auditable.module(),
                    auditable.targetType(),
                    null,
                    null,
                    requestMethod,
                    requestUrl,
                    requestParams,
                    responseCode,
                    ipAddress,
                    userAgent,
                    (int) duration,
                    auditable.remark()
            );
        }
    }
    
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
