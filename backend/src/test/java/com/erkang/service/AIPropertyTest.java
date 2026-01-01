package com.erkang.service;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI模块属性测试
 * 使用jqwik进行属性测试，每个测试至少100次迭代
 */
class AIPropertyTest {

    // 直接创建实例（不依赖数据库操作的方法可以直接测试）
    private final AIService aiService = new AIService(null, null, null);

    // ==================== Property 11: AI输入脱敏 ====================
    // **Validates: Requirements 10.6**

    @Property(tries = 100)
    void sanitizeInput_shouldMaskPhoneNumbers(
            @ForAll("phoneNumbers") String phone) {
        // Given: 包含手机号的输入
        String input = "我的电话是" + phone + "，请联系我";
        
        // When: 脱敏处理
        String result = aiService.sanitizeInput(input);
        
        // Then: 手机号应被脱敏
        assertThat(result).doesNotContain(phone);
        assertThat(result).contains("[手机号已隐藏]");
    }

    @Property(tries = 100)
    void sanitizeInput_shouldMaskIdCards(
            @ForAll("idCards") String idCard) {
        // Given: 包含身份证号的输入
        String input = "身份证号：" + idCard;
        
        // When: 脱敏处理
        String result = aiService.sanitizeInput(input);
        
        // Then: 身份证号应被脱敏
        assertThat(result).doesNotContain(idCard);
        assertThat(result).contains("[身份证号已隐藏]");
    }

    @Property(tries = 100)
    void sanitizeInput_shouldMaskPatientNames(
            @ForAll("chineseNames") String name) {
        // Given: 包含患者姓名的输入
        String input = "患者：" + name + "，年龄30岁";
        
        // When: 脱敏处理
        String result = aiService.sanitizeInput(input);
        
        // Then: 姓名应被部分脱敏（保留首字）
        assertThat(result).doesNotContain(name);
        assertThat(result).contains(name.charAt(0) + "*");
    }

    @Property(tries = 100)
    void sanitizeInput_shouldPreserveNonSensitiveContent(
            @ForAll("medicalSymptoms") String symptom) {
        // Given: 不包含敏感信息的医疗症状描述
        String input = "我感觉" + symptom;
        
        // When: 脱敏处理
        String result = aiService.sanitizeInput(input);
        
        // Then: 非敏感内容应保留
        assertThat(result).contains(symptom);
    }

    @Example
    void sanitizeInput_shouldHandleNullAndEmpty() {
        // Given: null或空输入
        // When & Then
        assertThat(aiService.sanitizeInput(null)).isEmpty();
        assertThat(aiService.sanitizeInput("")).isEmpty();
    }

    // ==================== Property 10: AI输出合规性 ====================
    // **Validates: Requirements 10.2, 10.3, 10.4**

    @Property(tries = 100)
    void ensureCompliance_shouldAddDisclaimer(
            @ForAll("aiResponses") String response) {
        // Given: AI原始响应
        // When: 合规校验
        String result = aiService.ensureCompliance(response, "普通问题");
        
        // Then: 应包含免责声明
        assertThat(result).contains("免责声明");
    }

    @Property(tries = 100)
    void ensureCompliance_shouldFilterDosageInfo(
            @ForAll("dosagePatterns") String dosage) {
        // Given: 包含剂量信息的响应
        String response = "建议服用药物，" + dosage;
        
        // When: 合规校验
        String result = aiService.ensureCompliance(response, "普通问题");
        
        // Then: 剂量信息应被过滤或包含遵医嘱提示
        boolean filtered = result.contains("[剂量请遵医嘱]") || !result.contains(dosage);
        assertThat(filtered).isTrue();
    }

    @Property(tries = 100)
    void ensureCompliance_shouldWarnEmergencySymptoms(
            @ForAll("emergencyKeywords") String keyword) {
        // Given: 包含急危重症关键词的问题
        String question = "我感觉" + keyword + "，怎么办？";
        
        // When: 合规校验
        String result = aiService.ensureCompliance("建议休息", question);
        
        // Then: 应包含紧急提醒
        assertThat(result).contains("紧急提醒");
        assertThat(result).contains("立即就医");
    }

    @Example
    void ensureCompliance_shouldNotDuplicateDisclaimer() {
        // Given: 已包含免责声明的响应
        String response = "建议休息。\n\n【免责声明】以上内容仅供参考。";
        
        // When: 合规校验
        String result = aiService.ensureCompliance(response, "普通问题");
        
        // Then: 不应重复添加免责声明
        int count = countOccurrences(result, "免责声明");
        assertThat(count).isEqualTo(1);
    }

    @Example
    void ensureCompliance_shouldHandleNullResponse() {
        // Given: null响应
        // When: 合规校验
        String result = aiService.ensureCompliance(null, "问题");
        
        // Then: 应返回免责声明
        assertThat(result).contains("免责声明");
    }

    // ==================== 数据生成器 ====================

    @Provide
    Arbitrary<String> phoneNumbers() {
        return Arbitraries.of("13", "14", "15", "16", "17", "18", "19")
                .flatMap(prefix -> Arbitraries.strings().numeric().ofLength(9)
                        .map(suffix -> prefix + suffix));
    }

    @Provide
    Arbitrary<String> idCards() {
        // 生成18位身份证号（简化版）
        return Arbitraries.strings().numeric().ofLength(17)
                .flatMap(prefix -> Arbitraries.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "X")
                        .map(suffix -> prefix + suffix));
    }

    @Provide
    Arbitrary<String> chineseNames() {
        // 常见中文姓名
        return Arbitraries.of(
                "张三", "李四", "王五", "赵六", "陈七",
                "刘伟", "杨芳", "黄丽", "周杰", "吴敏",
                "郑强", "孙静", "马超", "朱婷", "胡明"
        );
    }

    @Provide
    Arbitrary<String> medicalSymptoms() {
        return Arbitraries.of(
                "头痛", "耳鸣", "鼻塞", "咽痛", "咳嗽",
                "发热", "乏力", "眩晕", "听力下降", "流鼻涕"
        );
    }

    @Provide
    Arbitrary<String> aiResponses() {
        return Arbitraries.of(
                "建议多休息，多喝水",
                "这是常见症状，不必过于担心",
                "建议到医院进行检查",
                "可以尝试热敷缓解症状",
                "注意保持良好的作息习惯"
        );
    }

    @Provide
    Arbitrary<String> dosagePatterns() {
        return Arbitraries.of(
                "每次2片", "每日3次", "500mg", "100毫克",
                "每次1粒", "每日2次", "250mg", "50毫克"
        );
    }

    @Provide
    Arbitrary<String> emergencyKeywords() {
        return Arbitraries.of(
                "胸痛", "呼吸困难", "意识丧失", "大出血", "剧烈头痛",
                "高烧不退", "心悸", "窒息", "抽搐", "昏迷"
        );
    }

    // ==================== 辅助方法 ====================

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
