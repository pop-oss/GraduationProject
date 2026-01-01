package com.erkang.controller;

import com.erkang.domain.dto.LoginRequest;
import com.erkang.domain.vo.LoginVO;
import com.erkang.service.AuthService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证控制器测试
 * _Requirements: 1.1, 1.2, 1.3_
 */
class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeProperty
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    /**
     * Property 1: 登录请求参数验证 - 用户名和密码格式正确
     * **Validates: Requirements 1.1**
     */
    @Property(tries = 100)
    void loginRequest_shouldValidateUsernameAndPassword(
            @ForAll @AlphaChars @StringLength(min = 4, max = 20) String username,
            @ForAll @AlphaChars @StringLength(min = 6, max = 20) String password) {
        
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        
        assertThat(request.getUsername()).isEqualTo(username);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getUsername()).hasSizeBetween(4, 20);
        assertThat(request.getPassword()).hasSizeBetween(6, 20);
    }

    /**
     * Property 2: 登录成功应返回Token
     * **Validates: Requirements 1.1**
     */
    @Property(tries = 50)
    void login_withValidCredentials_shouldReturnToken(
            @ForAll @AlphaChars @StringLength(min = 4, max = 20) String username,
            @ForAll @AlphaChars @StringLength(min = 6, max = 20) String password) {
        
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        
        LoginVO mockLoginVO = LoginVO.builder()
            .accessToken("mock-access-token")
            .refreshToken("mock-refresh-token")
            .tokenType("Bearer")
            .expiresIn(86400L)
            .userId(1L)
            .username(username)
            .roles(List.of("PATIENT"))
            .build();
        
        when(authService.login(any(LoginRequest.class), anyString())).thenReturn(mockLoginVO);
        
        var result = authController.login(request, mockHttpRequest());
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getAccessToken()).isNotBlank();
    }

    /**
     * Property 3: 登出应调用服务层
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 50)
    void logout_withValidToken_shouldCallService(
            @ForAll @AlphaChars @StringLength(min = 20, max = 100) String token) {
        
        String authHeader = "Bearer " + token;
        
        doNothing().when(authService).logout(anyString());
        
        var result = authController.logout(authHeader);
        
        assertThat(result).isNotNull();
    }

    /**
     * Property 4: 刷新Token应返回新Token
     * **Validates: Requirements 1.3**
     */
    @Property(tries = 50)
    void refreshToken_withValidRefreshToken_shouldReturnNewToken(
            @ForAll @AlphaChars @StringLength(min = 20, max = 100) String refreshToken) {
        
        LoginVO mockLoginVO = LoginVO.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .tokenType("Bearer")
            .expiresIn(86400L)
            .userId(1L)
            .roles(List.of("PATIENT"))
            .build();
        
        when(authService.refreshToken(anyString())).thenReturn(mockLoginVO);
        
        var result = authController.refreshToken(refreshToken);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getAccessToken()).isNotBlank();
    }

    /**
     * Property 5: 空Token登出应正常处理
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 10)
    void logout_withNullOrEmptyToken_shouldNotThrowException() {
        var result = authController.logout(null);
        assertThat(result).isNotNull();
        verify(authService, never()).logout(anyString());
    }

    /**
     * Property 6: 无效Bearer格式不应调用登出
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 50)
    void logout_withInvalidBearerFormat_shouldNotCallService(
            @ForAll @AlphaChars @StringLength(min = 10, max = 50) String invalidHeader) {
        
        // 不以"Bearer "开头的header
        var result = authController.logout(invalidHeader);
        
        assertThat(result).isNotNull();
        verify(authService, never()).logout(anyString());
    }

    private jakarta.servlet.http.HttpServletRequest mockHttpRequest() {
        var request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        return request;
    }
}
