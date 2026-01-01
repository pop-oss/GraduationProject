package com.erkang.common.utils;

/**
 * 脱敏工具类
 */
public class DesensitizeUtil {
    
    /**
     * 手机号脱敏
     * 138****1234
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * 身份证号脱敏
     * 110***********1234
     */
    public static String desensitizeIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        int len = idCard.length();
        return idCard.substring(0, 3) + "***********" + idCard.substring(len - 4);
    }
    
    /**
     * 姓名脱敏
     * 张*明
     */
    public static String desensitizeName(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
    
    /**
     * 邮箱脱敏
     * t***@example.com
     */
    public static String desensitizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
    
    /**
     * 地址脱敏
     * 北京市朝阳区***
     */
    public static String desensitizeAddress(String address) {
        if (address == null || address.length() < 6) {
            return address;
        }
        return address.substring(0, Math.min(6, address.length())) + "***";
    }
}
