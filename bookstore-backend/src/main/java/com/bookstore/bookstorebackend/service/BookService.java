package com.bookstore.bookstorebackend.service;

import com.bookstore.bookstorebackend.entity.Book;
import com.bookstore.bookstorebackend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BookService {

    /** 新增书籍时如果没有填写封面颜色，就按顺序轮流使用这些颜色 */
    private static final String[] DEFAULT_COVER_COLORS = {
            "#5e81ac", "#a3be8c", "#ebcb8b", "#bf616a", "#b48ead", "#d08770"
    };

    @Autowired
    private BookRepository bookRepository;

    /**
     * 查询书籍列表
     *
     * @param filter all=全部（首页）、recommend=推荐、hot=热门
     */
    public List<Book> listBooks(String filter) {
        if ("recommend".equalsIgnoreCase(filter)) {
            return bookRepository.findByRecommendTrueOrderByIdAsc();
        }
        if ("hot".equalsIgnoreCase(filter)) {
            return bookRepository.findByHotTrueOrderByIdAsc();
        }
        return bookRepository.findAllByOrderByIdAsc();
    }

    /**
     * 管理员：查看全部书籍（和管理页共用同一份数据库数据）
     */
    public List<Book> listAllBooks() {
        return bookRepository.findAllByOrderByIdAsc();
    }

    /**
     * 管理员：加入书籍，保存进数据库后返回带 id 的书籍对象
     */
    public Book createBook(Book book) {
        LocalDateTime now = LocalDateTime.now();
        book.setId(null); // id 交给数据库自增
        book.setTitle(trim(book.getTitle()));
        book.setAuthor(trim(book.getAuthor()));
        if (book.getPrice() == null) {
            book.setPrice(BigDecimal.ZERO);
        }
        if (book.getIntro() != null) {
            book.setIntro(book.getIntro().trim());
        }
        if (book.getTags() == null) {
            book.setTags(new ArrayList<>());
        }
        if (book.getRecommend() == null) {
            book.setRecommend(false);
        }
        if (book.getHot() == null) {
            book.setHot(false);
        }
        if (book.getCoverColor() == null || book.getCoverColor().isBlank()) {
            book.setCoverColor(defaultCoverColor());
        }
        book.setCreateTime(now);
        book.setUpdateTime(now);
        return bookRepository.save(book);
    }

    /**
     * 管理员：修改书籍信息（价格、简介、书名、作者、封面颜色、推荐 / 热门标记）。
     *
     * 这里用 Map 接收前端传来的内容，只更新请求里“真正带上”的字段：
     * 比如只传了 price，就只改价格，其它字段保持数据库里的原值。
     * 注意不能用实体接收，因为实体的 Boolean 默认值是 false，
     * 漏传字段会被误判成“要把它改成 false”。
     *
     * @return 更新后的书籍，书籍不存在时返回 null
     */
    public Book updateBook(Long id, Map<String, Object> changes) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return null;
        }

        if (changes.containsKey("title")) {
            String title = asText(changes.get("title"));
            if (title != null && !title.isBlank()) {
                book.setTitle(title.trim());
            }
        }
        if (changes.containsKey("author")) {
            String author = asText(changes.get("author"));
            if (author != null && !author.isBlank()) {
                book.setAuthor(author.trim());
            }
        }
        if (changes.containsKey("price")) {
            BigDecimal price = parsePrice(changes.get("price"));
            if (price != null) {
                book.setPrice(price);
            }
        }
        if (changes.containsKey("intro")) {
            // 简介允许传空字符串，表示清空简介
            String intro = asText(changes.get("intro"));
            book.setIntro(intro == null ? null : intro.trim());
        }
        if (changes.containsKey("coverColor")) {
            String coverColor = asText(changes.get("coverColor"));
            if (coverColor != null && !coverColor.isBlank()) {
                book.setCoverColor(coverColor.trim());
            }
        }
        if (changes.containsKey("recommend") && changes.get("recommend") != null) {
            book.setRecommend(Boolean.parseBoolean(changes.get("recommend").toString()));
        }
        if (changes.containsKey("hot") && changes.get("hot") != null) {
            book.setHot(Boolean.parseBoolean(changes.get("hot").toString()));
        }

        book.setUpdateTime(LocalDateTime.now());
        return bookRepository.save(book);
    }

    /**
     * 管理员：删除书籍
     *
     * @return true=删除成功，false=这本书不存在
     */
    public boolean deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

    /**
     * 管理员：给某本书增加一个标签（已经存在的标签不会重复添加）
     *
     * @return 更新后的书籍，书籍不存在或标签为空时返回 null
     */
    public Book addTag(Long id, String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return null;
        }
        List<String> tags = book.getTags() == null
                ? new ArrayList<>()
                : new ArrayList<>(book.getTags());
        String cleaned = tag.trim();
        if (!tags.contains(cleaned)) {
            tags.add(cleaned);
        }
        book.setTags(tags);
        book.setUpdateTime(LocalDateTime.now());
        return bookRepository.save(book);
    }

    /**
     * 管理员：删除某本书的某个标签
     *
     * @return 更新后的书籍，书籍不存在或标签为空时返回 null
     */
    public Book removeTag(Long id, String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return null;
        }
        List<String> tags = book.getTags() == null
                ? new ArrayList<>()
                : new ArrayList<>(book.getTags());
        tags.remove(tag.trim());
        book.setTags(tags);
        book.setUpdateTime(LocalDateTime.now());
        return bookRepository.save(book);
    }

    /** 按数据库中已有书籍数量轮流取一个封面颜色 */
    private String defaultCoverColor() {
        long count = bookRepository.count();
        int index = (int) (count % DEFAULT_COVER_COLORS.length);
        return DEFAULT_COVER_COLORS[index];
    }

    /** 把前端传来的任意值当成文本读取 */
    private String asText(Object value) {
        return value == null ? null : value.toString();
    }

    /** 把前端传来的价格转成 BigDecimal，格式不对就返回 null（表示不改价格） */
    private BigDecimal parsePrice(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
