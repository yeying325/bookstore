package com.bookstore.bookstorebackend.controller;

import com.bookstore.bookstorebackend.entity.Book;
import com.bookstore.bookstorebackend.entity.User;
import com.bookstore.bookstorebackend.service.BookService;
import com.bookstore.bookstorebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员模式专用接口，路径都以 /api/admin 开头。
 * 这些接口必须先通过 AdminTokenInterceptor 的令牌校验。
 *
 * 书籍管理：
 *   GET    /api/admin/books            查看全部书籍
 *   POST   /api/admin/books            加入书籍（存进数据库）
 *   PUT    /api/admin/books/{id}       修改价格 / 简介 / 书名 / 作者等
 *   DELETE /api/admin/books/{id}       删除书籍
 *   POST   /api/admin/books/{id}/tags  给书籍加标签
 *   DELETE /api/admin/books/{id}/tags  删除书籍的某个标签
 *
 * 用户管理：
 *   GET    /api/admin/users            查看全部用户
 *   POST   /api/admin/users            创建用户
 *   DELETE /api/admin/users/{id}       删除用户
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    /* ==================== 书籍管理 ==================== */

    /** 查看全部书籍 */
    @GetMapping("/books")
    public Map<String, Object> listBooks() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", bookService.listAllBooks());
        return result;
    }

    /** 加入书籍：存入数据库，返回带 id 的新书 */
    @PostMapping("/books")
    public Map<String, Object> addBook(@RequestBody Book book) {
        if (book.getTitle() == null || book.getTitle().isBlank()) {
            return error(400, "书名不能为空");
        }
        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            return error(400, "作者不能为空");
        }
        Book saved = bookService.createBook(book);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "书籍已加入数据库");
        result.put("data", saved);
        return result;
    }

    /**
     * 修改书籍：价格、简介、书名、作者、封面颜色、推荐 / 热门标记。
     * 请求体里带上哪个字段就改哪个字段，例如 { "price": 45, "intro": "新简介" }
     */
    @PutMapping("/books/{id}")
    public Map<String, Object> updateBook(@PathVariable Long id, @RequestBody Map<String, Object> changes) {
        Book updated = bookService.updateBook(id, changes);
        if (updated == null) {
            return error(404, "这本书不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "修改成功");
        result.put("data", updated);
        return result;
    }

    /** 删除书籍 */
    @DeleteMapping("/books/{id}")
    public Map<String, Object> deleteBook(@PathVariable Long id) {
        if (!bookService.deleteBook(id)) {
            return error(404, "这本书不存在");
        }
        return success("书籍已删除");
    }

    /** 给书籍加标签，请求体：{ "tag": "科幻" } */
    @PostMapping("/books/{id}/tags")
    public Map<String, Object> addTag(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String tag = body == null ? null : body.get("tag");
        if (tag == null || tag.isBlank()) {
            return error(400, "标签不能为空");
        }
        Book book = bookService.addTag(id, tag);
        if (book == null) {
            return error(404, "这本书不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "标签已添加");
        result.put("data", book);
        return result;
    }

    /** 删除书籍的某个标签：/api/admin/books/3/tags?tag=科幻 */
    @DeleteMapping("/books/{id}/tags")
    public Map<String, Object> removeTag(@PathVariable Long id, @RequestParam String tag) {
        if (tag == null || tag.isBlank()) {
            return error(400, "标签不能为空");
        }
        Book book = bookService.removeTag(id, tag);
        if (book == null) {
            return error(404, "这本书不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "标签已删除");
        result.put("data", book);
        return result;
    }

    /* ==================== 用户管理 ==================== */

    /** 查看全部用户（不返回密码） */
    @GetMapping("/users")
    public Map<String, Object> listUsers() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (User user : userService.listUsers()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("email", user.getEmail());
            item.put("phone", user.getPhone());
            item.put("createTime", user.getCreateTime());
            data.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", data);
        return result;
    }

    /** 创建用户 */
    @PostMapping("/users")
    public Map<String, Object> addUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return error(400, "用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return error(400, "密码不能为空");
        }
        user.setUsername(user.getUsername().trim());
        if (userService.isReservedUsername(user.getUsername())) {
            return error(400, "该用户名为系统保留账号，请更换");
        }
        if (!userService.createUser(user)) {
            return error(400, "用户名已存在");
        }
        return success("用户创建成功");
    }

    /** 删除用户（管理员账号本身不能删） */
    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Integer id) {
        User target = userService.findById(id);
        if (target == null) {
            return error(404, "用户不存在");
        }
        if (userService.isReservedUsername(target.getUsername())) {
            return error(400, "系统管理员账号不能删除");
        }
        userService.deleteUser(id);
        return success("用户已删除");
    }

    /* ==================== 返回结果的小工具 ==================== */

    private Map<String, Object> success(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", message);
        return result;
    }

    private Map<String, Object> error(int code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        return result;
    }
}
