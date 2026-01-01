package com.erkang.security;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/**
 * JWT Token 属性测试
 * 
 * Feature: ent-telemedicine, Property 1: 认证 Token 有效性验证
 * Validates: Requirements 1.1, 1.2
 */
class JwtTokenProviderPropertyTest {
    
    private JwtTokenProvider jwtTokenProvider;
    
    @BeforeProperty
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // 设置测试用的密钥和过期时间
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", 
            "erkang-cloud-jwt-secret-key-2024-graduation-project-test");
        ReflectionTestUtils.setField(jwtTokenProvider, "expiration", 86400000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpiration", 604800000L);
    }
    
    /**
     * Property 1: 对于任意有效的用户信息，生成的Token应该能被正确验证
     */
    @Property(tries = 100)
    @Label("生成的Token应该是有效的")
    void generatedTokenShouldBeValid(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) Long userId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll("validRoles") List<String> roles
    ) {
        // Given: 有效的用户信息
        // When: 生成Token
        String token = jwtTokenProvider.generateToken(userId, username, roles);
        
        // Then: Token应该有效
        Assume.that(token != null && !token.isEmpty());
        assert jwtTokenProvider.validateToken(token) : "生成的Token应该是有效的";
    }
    
    /**
     * Property 2: 从Token中解析出的用户ID应该与生成时一致（Round-Trip）
     */
    @Property(tries = 100)
    @Label("Token中的用户ID应该保持一致")
    void userIdShouldBePreservedInToken(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) Long userId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll("validRoles") List<String> roles
    ) {
        // Given: 生成Token
        String token = jwtTokenProvider.generateToken(userId, username, roles);
        
        // When: 解析Token
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        
        // Then: 用户ID应该一致
        assert userId.equals(extractedUserId) : 
            String.format("用户ID不一致: 期望 %d, 实际 %d", userId, extractedUserId);
    }

    /**
     * Property 3: 从Token中解析出的用户名应该与生成时一致（Round-Trip）
     */
    @Property(tries = 100)
    @Label("Token中的用户名应该保持一致")
    void usernameShouldBePreservedInToken(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) Long userId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll("validRoles") List<String> roles
    ) {
        // Given: 生成Token
        String token = jwtTokenProvider.generateToken(userId, username, roles);
        
        // When: 解析Token
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        
        // Then: 用户名应该一致
        assert username.equals(extractedUsername) : 
            String.format("用户名不一致: 期望 %s, 实际 %s", username, extractedUsername);
    }
    
    /**
     * Property 4: 从Token中解析出的角色列表应该与生成时一致（Round-Trip）
     */
    @Property(tries = 100)
    @Label("Token中的角色列表应该保持一致")
    void rolesShouldBePreservedInToken(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) Long userId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll("validRoles") List<String> roles
    ) {
        // Given: 生成Token
        String token = jwtTokenProvider.generateToken(userId, username, roles);
        
        // When: 解析Token
        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);
        
        // Then: 角色列表应该一致
        assert roles.equals(extractedRoles) : 
            String.format("角色列表不一致: 期望 %s, 实际 %s", roles, extractedRoles);
    }
    
    /**
     * Property 5: 无效Token应该验证失败
     */
    @Property(tries = 100)
    @Label("无效Token应该验证失败")
    void invalidTokenShouldFailValidation(
            @ForAll @StringLength(min = 10, max = 100) String invalidToken
    ) {
        // Given: 随机字符串作为无效Token
        // When & Then: 验证应该失败
        assert !jwtTokenProvider.validateToken(invalidToken) : "无效Token不应该通过验证";
    }
    
    /**
     * 提供有效的角色列表
     */
    @Provide
    Arbitrary<List<String>> validRoles() {
        return Arbitraries.of(
            List.of("PATIENT"),
            List.of("DOCTOR_PRIMARY"),
            List.of("DOCTOR_EXPERT"),
            List.of("PHARMACIST"),
            List.of("ADMIN"),
            List.of("DOCTOR_PRIMARY", "DOCTOR_EXPERT")
        );
    }
}
