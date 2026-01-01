package com.erkang.common;

import lombok.Data;
import java.util.UUID;

/**
 * 统一返回体
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    
    /** 状态码：0成功，非0失败 */
    private int code;
    
    /** 提示信息 */
    private String message;
    
    /** 业务数据 */
    private T data;
    
    /** 追踪ID */
    private String traceId;
    
    private Result() {
        this.traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    public static <T> Result<T> success() {
        return success(null);
    }
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }
    
    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return fail(errorCode.getCode(), message);
    }
}
