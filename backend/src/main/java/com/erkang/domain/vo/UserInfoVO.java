package com.erkang.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息VO
 */
@Data
@Builder
public class UserInfoVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private List<String> roles;
    private LocalDateTime createdAt;
}
