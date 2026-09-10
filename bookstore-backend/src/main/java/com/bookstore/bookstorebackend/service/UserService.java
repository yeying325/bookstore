package com.bookstore.bookstorebackend.service;

import com.bookstore.bookstorebackend.entity.User;
import com.bookstore.bookstorebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user; // 用户名密码正确，返回用户信息
        }
        return null; // 验证失败返回null
    }

    /**
     * 根据用户名查找用户（用于“我的”页面展示资料）
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 用户注册
     */
    public boolean register(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            return false; // 用户名已存在，注册失败
        }
        // 设置创建时间
        user.setCreateTime(LocalDateTime.now());
        userRepository.save(user); // 保存到数据库
        return true; // 注册成功
    }
}
