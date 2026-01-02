package com.erkang.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
        log.warn("业务异常: {} - {} (code: {})", request.getRequestURI(), e.getMessage(), e.getCode());
        
        // 根据错误码设置 HTTP 状态码
        int code = e.getCode();
        if (code == ErrorCode.UNAUTHORIZED.getCode() || code == ErrorCode.AUTH_TOKEN_EXPIRED.getCode() 
                || code == ErrorCode.AUTH_TOKEN_INVALID.getCode()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        } else if (code == ErrorCode.FORBIDDEN.getCode() || code == ErrorCode.AUTH_ROLE_NOT_ALLOWED.getCode()
                || code == ErrorCode.AUTH_DATA_SCOPE_DENIED.getCode()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        } else if (code == ErrorCode.NOT_FOUND.getCode() || (code >= 2001 && code <= 8999 && e.getMessage().contains("不存在"))) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } else if (code == ErrorCode.PARAM_ERROR.getCode()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
        
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数校验异常 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ErrorCode.PARAM_ERROR, "参数校验失败: " + message);
    }
    
    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.fail(ErrorCode.PARAM_ERROR, "参数绑定失败: " + message);
    }
    
    /**
     * 权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.fail(ErrorCode.FORBIDDEN);
    }
    
    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.fail(ErrorCode.PARAM_ERROR, e.getMessage());
    }
    
    /**
     * 未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.fail(ErrorCode.SYSTEM_BUSY);
    }
}
