package com.bookstore.bookstorebackend.service;

import com.bookstore.bookstorebackend.entity.User;
import com.bookstore.bookstorebackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员模式相关逻辑。
 *
 * 目前项目没有引入 Spring Security，所以这里用最直白的方式实现：
 * 固定账号 + 固定密码 + 一个固定令牌。
 * 前端登录成功后拿到令牌，调用 /api/admin/** 接口时带上 X-Admin-Token 请求头，
 * 后端 AdminTokenInterceptor 会校验这个请求头，非管理员拿不到管理员数据。
 */
@Service
public class AdminService {

    /** 管理员账号 */
    public static final String ADMIN_USERNAME = "admin";

    /** 管理员密码 */
    public static final String ADMIN_PASSWORD = "bookstore";

    /** 管理员令牌：登录成功后发给前端，管理员接口都要带上它 */
    private static final String ADMIN_TOKEN = "bookstore-admin-token";

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * 校验是不是管理员账号密码
     */
    public boolean isAdminCredentials(String username, String password) {
        return username != null && password != null
                && ADMIN_USERNAME.equals(username.trim())
                && ADMIN_PASSWORD.equals(password);
    }

    /**
     * 登录成功后发给前端的令牌
     */
    public String getToken() {
        return ADMIN_TOKEN;
    }

    /**
     * 校验请求头里的令牌
     */
    public boolean isValidToken(String token) {
        return ADMIN_TOKEN.equals(token);
    }

    /**
     * 保证数据库的 users 表里有 admin 这条记录。
     * 这样管理员也能像普通用户一样打开“我的”页面看资料、收藏书籍。
     * 表中已经有 admin 用户时不做任何修改（不会覆盖已有密码）。
     */
    public void ensureAdminUser() {
        try {
            if (userRepository.findByUsername(ADMIN_USERNAME) != null) {
                return;
            }
            User admin = new User();
            admin.setUsername(ADMIN_USERNAME);
            admin.setPassword(ADMIN_PASSWORD);
            admin.setCreateTime(LocalDateTime.now());
            userRepository.save(admin);
        } catch (Exception e) {
            // 数据库暂时不可用时不影响管理员登录，只是“我的”页面没有这条记录
            log.warn("自动创建管理员用户记录失败：{}", e.getMessage());
        }
    }
}
