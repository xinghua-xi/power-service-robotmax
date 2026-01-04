package com.powerservice.system.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类，用于生成BCrypt密码哈希
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        // 要生成哈希的明文密码
        String password = "password";
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);
        
        System.out.println("明文密码: " + password);
        System.out.println("BCrypt哈希: " + hashedPassword);
        System.out.println("密码长度: " + hashedPassword.length());
        
        // 验证密码是否匹配
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("密码匹配: " + matches);
        
        // 验证数据库中的哈希值是否匹配
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean dbMatches = encoder.matches(password, dbHash);
        System.out.println("数据库哈希匹配: " + dbMatches);
    }
}