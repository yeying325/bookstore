package com.bookstore.bookstorebackend.controller;

import com.bookstore.bookstorebackend.entity.Book;
import com.bookstore.bookstorebackend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 书籍接口：数据全部来自 MySQL 的 books 表。
 *
 * GET /api/books                    首页：全部书籍
 * GET /api/books?filter=recommend   推荐：is_recommend = 1
 * GET /api/books?filter=hot         热门：is_hot = 1
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public Map<String, Object> listBooks(
            @RequestParam(value = "filter", required = false, defaultValue = "all") String filter) {

        List<Book> books = bookService.listBooks(filter);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", books);
        return result;
    }
}
