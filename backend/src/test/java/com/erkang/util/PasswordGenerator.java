package com.erkang.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 123456 的哈希
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("=== 123456 ===");
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
        System.out.println("Verification: " + encoder.matches(rawPassword, encodedPassword));
        
        // 生成 admin123 的哈希
        String adminPassword = "admin123";
        String adminEncoded = encoder.encode(adminPassword);
        System.out.println("\n=== admin123 ===");
        System.out.println("Raw password: " + adminPassword);
        System.out.println("Encoded password: " + adminEncoded);
        System.out.println("Verification: " + encoder.matches(adminPassword, adminEncoded));
        
        // 验证现有哈希
        String existingHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        System.out.println("\n=== 验证现有哈希 ===");
        System.out.println("Hash: " + existingHash);
        System.out.println("matches 'password': " + encoder.matches("password", existingHash));
        System.out.println("matches '123456': " + encoder.matches("123456", existingHash));
        System.out.println("matches 'admin123': " + encoder.matches("admin123", existingHash));
    }
}
