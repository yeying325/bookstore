package com.bookstore.bookstorebackend.service;

import com.bookstore.bookstorebackend.entity.User;
import com.bookstore.bookstorebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
     * 根据 id 查找用户（管理员删除用户前用来判断这个人是谁）
     */
    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 是否是系统保留账号（管理员账号只能由系统使用，不能被注册/删除）
     */
    public boolean isReservedUsername(String username) {
        return username != null && AdminService.ADMIN_USERNAME.equalsIgnoreCase(username.trim());
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

    /**
     * 管理员：查看全部用户（按 id 升序）
     */
    public List<User> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * 管理员：创建用户，规则和注册接口一致
     *
     * @return true=创建成功，false=用户名已存在
     */
    public boolean createUser(User user) {
        return register(user);
    }

    /**
     * 管理员：删除用户。管理员账号本身不允许删除。
     *
     * @return true=删除成功，false=用户不存在或是保留账号
     */
    public boolean deleteUser(Integer id) {
        User user = findById(id);
        if (user == null || isReservedUsername(user.getUsername())) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
