package com.bookstore.bookstorebackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把管理员令牌拦截器挂到 /api/admin/** 上
 */
@Configuration
public class AdminAuthConfig implements WebMvcConfigurer {

    @Autowired
    private AdminTokenInterceptor adminTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminTokenInterceptor).addPathPatterns("/api/admin/**");
    }
}
