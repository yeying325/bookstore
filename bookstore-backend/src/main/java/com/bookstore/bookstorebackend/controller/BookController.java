package com.bookstore.bookstorebackend.controller;

import com.bookstore.bookstorebackend.entity.Book;
import com.bookstore.bookstorebackend.service.BookService;
import com.bookstore.bookstorebackend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 书籍接口：数据全部来自 MySQL 的 books 表。
 *
 * GET /api/books                    首页：全部书籍
 * GET /api/books?filter=recommend   推荐：is_recommend = 1
 * GET /api/books?filter=hot         热门：is_hot = 1
 * GET /api/books/3                  详情页：某一本书的详细信息 + 评分统计
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ReviewService reviewService;

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

    /**
     * 书籍详情：返回这本书的信息，外加评价条数 / 平均分，
     * 详情页可以直接用来展示“评分 4.5 分，共 3 条评价”。
     */
    @GetMapping("/{id}")
    public Map<String, Object> getBook(@PathVariable Long id) {
        Book book = bookService.getBook(id);
        if (book == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "这本书不存在，可能已经被管理员删除");
            return result;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("book", book);
        data.put("reviewSummary", reviewService.summary(id));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", data);
        return result;
    }
}
