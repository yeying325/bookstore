package com.bookstore.bookstorebackend.config;

import com.bookstore.bookstorebackend.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 管理员接口的“门卫”：
 * 所有 /api/admin/** 的请求都必须带上登录时拿到的 X-Admin-Token 请求头，
 * 否则直接返回 403，前端会提示“无管理员权限”。
 */
@Component
public class AdminTokenInterceptor implements HandlerInterceptor {

    /** 管理员令牌放在这个请求头里 */
    public static final String TOKEN_HEADER = "X-Admin-Token";

    @Autowired
    private AdminService adminService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        // 浏览器的跨域预检请求要放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(TOKEN_HEADER);
        if (adminService.isValidToken(token)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"无管理员权限，请使用管理员账号登录\"}");
        return false;
    }
}
