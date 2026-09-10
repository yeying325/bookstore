package com.bookstore.bookstorebackend.controller;

import com.bookstore.bookstorebackend.entity.User;
import com.bookstore.bookstorebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * “我的”页面：返回当前用户的个人资料。
 * 邮箱和手机号只返回脱敏后的内容，避免完整信息暴露在前端。
 */
@RestController
@RequestMapping("/api/user")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public Map<String, Object> getProfile(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();

        User user = userService.findByUsername(username);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("emailMasked", maskEmail(user.getEmail()));
        data.put("phoneMasked", maskPhone(user.getPhone()));

        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", data);
        return result;
    }

    /**
     * 邮箱脱敏：只保留第一个字符和 @ 后的域名，如 user123@qq.com -> u***@qq.com
     */
    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        email = email.trim();
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return maskGeneric(email);
        }

        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (name.length() <= 1) {
            return name + "***" + domain;
        }
        return name.charAt(0) + "***" + domain;
    }

    /**
     * 手机号脱敏：保留前 3 位和后 4 位，如 13812345678 -> 138****5678
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("1")) {
            return digits.substring(0, 3) + "****" + digits.substring(7);
        }
        return maskGeneric(digits.isEmpty() ? phone : digits);
    }

    /**
     * 其他格式兜底：保留第一位，其余用 * 代替
     */
    private String maskGeneric(String value) {
        if (value == null || value.length() <= 1) {
            return value;
        }
        return value.charAt(0) + "*".repeat(value.length() - 1);
    }
}
