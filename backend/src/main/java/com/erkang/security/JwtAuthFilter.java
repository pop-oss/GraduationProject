package com.erkang.security;

import com.erkang.service.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    /** 白名单路径 */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/auth/refresh",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**"
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // 白名单放行
        if (isWhiteListed(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        try {
            String token = extractToken(httpRequest);
            
            if (token == null) {
                sendUnauthorized(httpResponse, "未提供认证Token");
                return;
            }
            
            // 检查Token是否在黑名单
            if (authService.isTokenBlacklisted(token)) {
                sendUnauthorized(httpResponse, "Token已失效");
                return;
            }
            
            // 验证Token
            if (!jwtTokenProvider.validateToken(token)) {
                sendUnauthorized(httpResponse, "Token无效或已过期");
                return;
            }
            
            // 设置用户上下文
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            List<String> roles = jwtTokenProvider.getRolesFromToken(token);
            
            LoginUser loginUser = LoginUser.builder()
                    .userId(userId)
                    .username(username)
                    .roles(roles)
                    .build();
            UserContext.setUser(loginUser);
            
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 从请求头提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 检查是否在白名单
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    /**
     * 发送401响应
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"code\":1001,\"message\":\"%s\",\"data\":null}", message)
        );
    }
}
