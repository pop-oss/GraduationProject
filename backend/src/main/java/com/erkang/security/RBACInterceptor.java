package com.erkang.security;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * RBAC权限拦截器
 */
@Slf4j
@Component
public class RBACInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        
        // 获取方法或类上的@RequireRole注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        
        // 没有注解，放行
        if (requireRole == null) {
            return true;
        }
        
        // 获取当前用户
        LoginUser user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 检查角色权限
        String[] allowedRoles = requireRole.value();
        if (!user.hasAnyRole(allowedRoles)) {
            log.warn("用户 {} 无权访问 {}, 需要角色: {}, 当前角色: {}", 
                    user.getUsername(), 
                    request.getRequestURI(),
                    String.join(",", allowedRoles),
                    String.join(",", user.getRoles()));
            throw new BusinessException(ErrorCode.AUTH_ROLE_NOT_ALLOWED);
        }
        
        return true;
    }
}
