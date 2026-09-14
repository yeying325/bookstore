package com.bookstore.bookstorebackend.controller;

import com.bookstore.bookstorebackend.entity.Review;
import com.bookstore.bookstorebackend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 书籍详情页的评价接口，路径都以 /api/books/{bookId}/reviews 开头。
 *
 * 查询评价：GET    /api/books/3/reviews
 * 发表评价：POST   /api/books/3/reviews
 * 删除自己的评价：DELETE /api/books/3/reviews/8?username=tom
 *
 * 管理员删除别人的评价走 /api/admin/reviews/{id}，需要 X-Admin-Token 请求头。
 */
@RestController
@RequestMapping("/api/books/{bookId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /** 查看某本书的评价列表和评分统计 */
    @GetMapping
    public Map<String, Object> listReviews(@PathVariable Long bookId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reviews", reviewService.listByBook(bookId));
        data.put("summary", reviewService.summary(bookId));
        return ok("获取成功", data);
    }

    /** 发表评价，请求体：{ "username": "tom", "rating": 5, "content": "很好看" } */
    @PostMapping
    public Map<String, Object> addReview(@PathVariable Long bookId,
                                         @RequestBody Map<String, Object> body) {
        try {
            Review saved = reviewService.saveReview(
                    bookId,
                    asText(body.get("username")),
                    asRating(body.get("rating")),
                    asText(body.get("content")));
            return ok("评价已保存", saved);
        } catch (IllegalArgumentException e) {
            return error(400, e.getMessage());
        }
    }

    /** 删除自己的评价（别人的评价删不掉，前端也会把按钮藏起来） */
    @DeleteMapping("/{reviewId}")
    public Map<String, Object> deleteOwnReview(@PathVariable Long bookId,
                                               @PathVariable Long reviewId,
                                               @RequestParam(required = false) String username) {
        if (!reviewService.deleteOwnReview(bookId, reviewId, username)) {
            return error(403, "只能删除自己写的评价");
        }
        return success("评价已删除");
    }

    /** 把请求体里的值安全地读成文本 */
    private String asText(Object value) {
        return value == null ? null : value.toString();
    }

    /** 把请求体里的评分读成整数，格式不对返回 null（服务层按默认 5 星处理） */
    private Integer asRating(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, Object> ok(String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

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
