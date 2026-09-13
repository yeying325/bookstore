package com.bookstore.bookstorebackend.controller;

import com.bookstore.bookstorebackend.entity.User;
import com.bookstore.bookstorebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * “我的”页面：查看和修改当前用户的联系方式。
 *
 * 查看资料：GET /api/user/profile   邮箱、手机号脱敏后返回，页面上直接展示
 * 打开编辑：GET /api/user/contact   返回未脱敏的原值，用来填进输入框
 * 保存修改：PUT /api/user/contact   保存新的邮箱、手机号（注册时没填的也能补上）
 */
@RestController
@RequestMapping("/api/user")
public class ProfileController {

    /** 邮箱格式：xxx@yyy.zzz，做个基本校验即可 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** 手机号格式：11 位，1 开头，第二位是 3-9 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 邮箱长度上限，避免超出数据库字段长度 */
    private static final int EMAIL_MAX_LENGTH = 100;

    @Autowired
    private UserService userService;

    /** 查看个人资料：邮箱和手机号都是脱敏后的内容 */
    @GetMapping("/profile")
    public Map<String, Object> getProfile(@RequestParam String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return error(404, "用户不存在");
        }
        return ok("获取成功", buildProfileData(user));
    }

    /**
     * 点“修改”时读取真实的邮箱和手机号，用于填充输入框。
     * 页面平时只展示脱敏信息，只有主动编辑才会取到完整内容。
     */
    @GetMapping("/contact")
    public Map<String, Object> getContact(@RequestParam String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return error(404, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        return ok("获取成功", data);
    }

    /**
     * 修改邮箱 / 手机号：请求体里带上哪个字段就改哪个。
     * 例如 { "username": "tom", "email": "tom@qq.com", "phone": "" }
     * 字段传空字符串表示解除绑定（两个都传空就等于清空联系方式）。
     */
    @PutMapping("/contact")
    public Map<String, Object> updateContact(@RequestBody Map<String, Object> body) {
        String username = asText(body.get("username")).trim();
        if (username.isEmpty()) {
            return error(400, "缺少用户名，请重新登录后再试");
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            return error(404, "用户不存在");
        }

        Map<String, Object> changes = new HashMap<>();

        if (body.containsKey("email")) {
            String email = asText(body.get("email")).trim();
            if (!email.isEmpty()) {
                if (email.length() > EMAIL_MAX_LENGTH) {
                    return error(400, "邮箱太长了，请换一个");
                }
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    return error(400, "邮箱格式不正确，请检查后重试");
                }
            }
            changes.put("email", email);
        }

        if (body.containsKey("phone")) {
            String phone = asText(body.get("phone")).trim();
            if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
                return error(400, "手机号格式不正确，请输入 11 位手机号");
            }
            changes.put("phone", phone);
        }

        if (changes.isEmpty()) {
            return error(400, "没有需要修改的内容");
        }

        User updated = userService.updateContact(username, changes);
        if (updated == null) {
            return error(404, "用户不存在");
        }
        // 返回最新的脱敏资料，前端可以直接刷新“我的”页面
        return ok("联系方式已更新", buildProfileData(updated));
    }

    /** “我的”页面展示用的资料（联系方式脱敏） */
    private Map<String, Object> buildProfileData(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("emailMasked", maskEmail(user.getEmail()));
        data.put("phoneMasked", maskPhone(user.getPhone()));
        return data;
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

    /* ==================== 返回结果的小工具 ==================== */

    /** 把请求体里的值安全地读成文本，null 当成空字符串 */
    private String asText(Object value) {
        return value == null ? "" : value.toString();
    }

    private Map<String, Object> ok(String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> error(int code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        return result;
    }
}
