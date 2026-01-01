package com.erkang.common;

import com.erkang.common.utils.DesensitizeUtil;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

/**
 * 脱敏工具属性测试
 * 
 * Feature: ent-telemedicine, Property 8: 患者档案脱敏
 * Validates: Requirements 2.4
 */
class DesensitizePropertyTest {
    
    /**
     * Property 1: 手机号脱敏后应该隐藏中间4位
     * 对于任意有效手机号，脱敏后格式应为 138****1234
     */
    @Property(tries = 100)
    @Label("手机号脱敏隐藏中间4位")
    void phoneDesensitizationHidesMiddleDigits(
            @ForAll("validPhoneNumbers") String phone
    ) {
        // When: 脱敏
        String desensitized = DesensitizeUtil.desensitizePhone(phone);
        
        // Then: 格式正确
        assert desensitized.length() == 11 : "脱敏后长度应为11";
        assert desensitized.substring(0, 3).equals(phone.substring(0, 3)) : "前3位应保留";
        assert desensitized.substring(3, 7).equals("****") : "中间4位应为****";
        assert desensitized.substring(7).equals(phone.substring(7)) : "后4位应保留";
        
        // 原始信息不应完整暴露
        assert !desensitized.equals(phone) : "脱敏后不应与原始值相同";
    }
    
    /**
     * Property 2: 身份证号脱敏后应该隐藏中间部分
     * 对于任意有效身份证号，脱敏后应隐藏中间11位
     */
    @Property(tries = 100)
    @Label("身份证号脱敏隐藏中间部分")
    void idCardDesensitizationHidesMiddle(
            @ForAll("validIdCards") String idCard
    ) {
        // When: 脱敏
        String desensitized = DesensitizeUtil.desensitizeIdCard(idCard);
        
        // Then: 格式正确
        assert desensitized.startsWith(idCard.substring(0, 3)) : "前3位应保留";
        assert desensitized.endsWith(idCard.substring(idCard.length() - 4)) : "后4位应保留";
        assert desensitized.contains("***") : "中间应有***";
        
        // 原始信息不应完整暴露
        assert !desensitized.equals(idCard) : "脱敏后不应与原始值相同";
    }
    
    /**
     * Property 3: 脱敏后的数据不应包含完整的原始敏感信息
     */
    @Property(tries = 100)
    @Label("脱敏后不包含完整原始信息")
    void desensitizedDataShouldNotContainOriginal(
            @ForAll("validPhoneNumbers") String phone,
            @ForAll("validIdCards") String idCard
    ) {
        String desensitizedPhone = DesensitizeUtil.desensitizePhone(phone);
        String desensitizedIdCard = DesensitizeUtil.desensitizeIdCard(idCard);
        
        // 脱敏后的数据不应等于原始数据
        assert !desensitizedPhone.equals(phone) : "手机号脱敏后不应与原始值相同";
        assert !desensitizedIdCard.equals(idCard) : "身份证号脱敏后不应与原始值相同";
        
        // 脱敏后应包含*号
        assert desensitizedPhone.contains("*") : "手机号脱敏后应包含*";
        assert desensitizedIdCard.contains("*") : "身份证号脱敏后应包含*";
    }
    
    /**
     * Property 4: 空值或短字符串应该安全处理
     */
    @Property(tries = 100)
    @Label("空值和短字符串安全处理")
    void nullAndShortStringsShouldBeSafe(
            @ForAll @StringLength(max = 6) String shortString
    ) {
        // When: 脱敏短字符串
        String desensitizedPhone = DesensitizeUtil.desensitizePhone(shortString);
        String desensitizedIdCard = DesensitizeUtil.desensitizeIdCard(shortString);
        
        // Then: 不应抛出异常，返回原值或安全值
        // 短字符串直接返回原值
        if (shortString == null || shortString.length() < 7) {
            assert desensitizedPhone == null || desensitizedPhone.equals(shortString);
        }
        if (shortString == null || shortString.length() < 8) {
            assert desensitizedIdCard == null || desensitizedIdCard.equals(shortString);
        }
    }

    /**
     * Property 5: 姓名脱敏应该保留首尾字符
     */
    @Property(tries = 100)
    @Label("姓名脱敏保留首尾")
    void nameDesensitizationPreservesFirstAndLast(
            @ForAll("validNames") String name
    ) {
        // When: 脱敏
        String desensitized = DesensitizeUtil.desensitizeName(name);
        
        // Then: 首尾保留
        if (name.length() >= 2) {
            assert desensitized.charAt(0) == name.charAt(0) : "首字符应保留";
            if (name.length() > 2) {
                assert desensitized.charAt(desensitized.length() - 1) == name.charAt(name.length() - 1) : "尾字符应保留";
                assert desensitized.contains("*") : "中间应有*";
            }
        }
    }
    
    @Provide
    Arbitrary<String> validPhoneNumbers() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .ofLength(11)
                .filter(s -> s.startsWith("1"));
    }
    
    @Provide
    Arbitrary<String> validIdCards() {
        // 生成18位身份证号格式的字符串
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .ofLength(18);
    }
    
    @Provide
    Arbitrary<String> validNames() {
        return Arbitraries.of(
            "张三", "李四", "王五", "赵六",
            "张三丰", "欧阳锋", "司马懿",
            "诸葛亮", "上官婉儿"
        );
    }
}
