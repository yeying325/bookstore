package com.bookstore.bookstorebackend.service;

import com.bookstore.bookstorebackend.entity.Book;
import com.bookstore.bookstorebackend.entity.Review;
import com.bookstore.bookstorebackend.repository.BookRepository;
import com.bookstore.bookstorebackend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 书籍评价相关的业务逻辑。
 *
 * 普通用户：查看某本书的评价、发表 / 修改自己的评价、删除自己的评价
 * 管理员：查看全部评价、删除任意一条评价
 */
@Service
public class ReviewService {

    /** 评分范围：1-5 星 */
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    /** 评价内容长度上限，和数据库 varchar(500) 保持一致 */
    private static final int MAX_CONTENT_LENGTH = 500;

    /** 用户名长度上限，和数据库 varchar(50) 保持一致 */
    private static final int MAX_USERNAME_LENGTH = 50;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    /** 某本书的全部评价，最新的排在最前面 */
    public List<Review> listByBook(Long bookId) {
        return reviewRepository.findByBookIdOrderByIdDesc(bookId);
    }

    /**
     * 某本书的评分统计，直接由评价列表算出来（评价数量不大，逻辑简单直观）。
     *
     * 返回内容：评价条数、平均分（保留一位小数）、每个星级各有多少条。
     */
    public Map<String, Object> summary(Long bookId) {
        List<Review> reviews = reviewRepository.findByBookIdOrderByIdDesc(bookId);
        int count = reviews.size();

        int total = 0;
        Map<Integer, Integer> distribution = new LinkedHashMap<>();
        for (int star = MAX_RATING; star >= MIN_RATING; star--) {
            distribution.put(star, 0);
        }
        for (Review review : reviews) {
            int rating = review.getRating() == null ? MAX_RATING : review.getRating();
            total += rating;
            distribution.merge(rating, 1, Integer::sum);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", count);
        data.put("average", count == 0 ? 0.0 : round1((double) total / count));
        data.put("distribution", distribution);
        return data;
    }

    /**
     * 发表评价：同一个用户在同一本书下只保留一条，再次提交就是修改原来那条。
     *
     * @return 保存后的评价
     * @throws IllegalArgumentException 参数不合法时抛出，提示语可以直接展示给用户
     */
    public Review saveReview(Long bookId, String username, Integer rating, String content) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("请先登录后再发表评价");
        }
        if (name.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("用户名太长，无法发表评价");
        }
        if (!bookRepository.existsById(bookId)) {
            throw new IllegalArgumentException("这本书不存在，可能已经被管理员删除");
        }

        int stars = rating == null ? MAX_RATING : rating;
        if (stars < MIN_RATING || stars > MAX_RATING) {
            throw new IllegalArgumentException("评分需要在 1-5 星之间");
        }

        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("评价内容不能为空");
        }
        if (text.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("评价内容最多 " + MAX_CONTENT_LENGTH + " 个字");
        }

        LocalDateTime now = LocalDateTime.now();
        Review review = reviewRepository.findFirstByBookIdAndUsername(bookId, name);
        if (review == null) {
            review = new Review();
            review.setBookId(bookId);
            review.setUsername(name);
            review.setCreateTime(now);
        }
        review.setRating(stars);
        review.setContent(text);
        review.setUpdateTime(now);
        return reviewRepository.save(review);
    }

    /**
     * 普通用户删除自己的评价（别人的评价删不掉）。
     *
     * @return true=删除成功，false=评价不存在或不属于这个人
     */
    public boolean deleteOwnReview(Long bookId, Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !review.getBookId().equals(bookId)) {
            return false;
        }
        String name = username == null ? "" : username.trim();
        if (name.isEmpty() || !review.getUsername().equalsIgnoreCase(name)) {
            return false;
        }
        reviewRepository.delete(review);
        return true;
    }

    /**
     * 管理员删除任意一条评价
     *
     * @return true=删除成功，false=评价不存在
     */
    public boolean deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            return false;
        }
        reviewRepository.deleteById(reviewId);
        return true;
    }

    /** 管理员：查看全部评价（带上书名，方便在管理页面直接辨认是哪本书） */
    public List<Map<String, Object>> listAllReviews() {
        Map<Long, String> bookTitles = new HashMap<>();
        for (Book book : bookRepository.findAllByOrderByIdAsc()) {
            bookTitles.put(book.getId(), book.getTitle());
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Review review : reviewRepository.findAllByOrderByIdDesc()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", review.getId());
            item.put("bookId", review.getBookId());
            item.put("bookTitle", bookTitles.getOrDefault(review.getBookId(), "已删除的书籍"));
            item.put("username", review.getUsername());
            item.put("rating", review.getRating());
            item.put("content", review.getContent());
            item.put("createTime", review.getCreateTime());
            item.put("updateTime", review.getUpdateTime());
            data.add(item);
        }
        return data;
    }

    /** 平均分保留一位小数，如 4.3333 -> 4.3 */
    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
