package com.erkang.service;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.dto.LoginRequest;
import com.erkang.domain.entity.User;
import com.erkang.domain.vo.LoginVO;
import com.erkang.mapper.UserMapper;
import com.erkang.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    /**
     * 用户登录
     */
    public LoginVO login(LoginRequest request, String ip) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            log.warn("用户不存在: {}", request.getUsername());
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            log.warn("用户已禁用: {}", request.getUsername());
            throw new BusinessException(ErrorCode.AUTH_USER_DISABLED);
        }
        
        // 验证密码
        log.info("验证密码 - 用户: {}, 输入密码: {}, 数据库哈希: {}", 
                 request.getUsername(), request.getPassword(), user.getPassword());
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("密码验证结果: {}", matches);
        
        if (!matches) {
            log.warn("密码验证失败: {}", request.getUsername());
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        
        // 获取用户角色
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        
        // 生成Token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // 更新登录信息
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);
        
        log.info("用户登录成功: userId={}, username={}, ip={}", user.getId(), user.getUsername(), ip);
        
        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roles)
                .build();
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 将Token加入黑名单
            String key = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", expiration, TimeUnit.MILLISECONDS);
            log.info("用户登出，Token已加入黑名单");
        }
    }
    
    /**
     * 刷新Token
     */
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID);
        }
        
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.AUTH_USER_DISABLED);
        }
        
        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        String newAccessToken = jwtTokenProvider.generateToken(userId, user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
        
        return LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roles)
                .build();
    }
    
    /**
     * 检查Token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 加密密码
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
