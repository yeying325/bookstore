package com.bookstore.bookstorebackend.service;

import com.bookstore.bookstorebackend.entity.Book;
import com.bookstore.bookstorebackend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

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
}
