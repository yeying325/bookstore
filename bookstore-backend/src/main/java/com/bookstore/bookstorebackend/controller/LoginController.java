package com.bookstore.bookstorebackend.controller;

import com.bookstore.bookstorebackend.entity.User;
import com.bookstore.bookstorebackend.service.AdminService;
import com.bookstore.bookstorebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    /**
     * 登录接口
     * 普通用户：校验 users 表里的用户名和密码
     * 管理员：账号 admin + 密码 bookstore，登录后返回 role=admin 和 adminToken
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 1. 先判断是不是管理员账号
        if (adminService.isAdminCredentials(username, password)) {
            // 保证 users 表里有 admin 这条记录，管理员才能正常使用“我的”页面
            adminService.ensureAdminUser();

            Map<String, Object> data = new HashMap<>();
            data.put("username", AdminService.ADMIN_USERNAME);
            data.put("role", "admin");
            // 前端拿到这个令牌后，调用管理员接口时放在 X-Admin-Token 请求头里
            data.put("adminToken", adminService.getToken());

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "管理员登录成功");
            result.put("data", data);
            return result;
        }

        // 2. 普通用户登录
        User user = userService.login(username, password);

        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("code", 200);
            result.put("message", "登录成功");
            // 这里可以返回一些基本的用户信息给前端
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("role", "user");
            result.put("data", data);
        } else {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
        }
        return result;
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();

        // admin 是系统保留账号，不能被注册
        if (userService.isReservedUsername(user.getUsername())) {
            result.put("code", 400);
            result.put("message", "该用户名为系统保留账号，请更换");
            return result;
        }

        boolean success = userService.register(user);
        if (success) {
            result.put("code", 200);
            result.put("message", "注册成功");
        } else {
            result.put("code", 400);
            result.put("message", "用户名已存在");
        }
        return result;
    }
}
