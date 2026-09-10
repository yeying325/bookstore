package com.bookstore.bookstorebackend.repository;

import com.bookstore.bookstorebackend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /** 全部书籍，按 id 升序（首页用） */
    List<Book> findAllByOrderByIdAsc();

    /** 推荐书籍（“推荐”页用） */
    List<Book> findByRecommendTrueOrderByIdAsc();

    /** 热门书籍（“热门”页用） */
    List<Book> findByHotTrueOrderByIdAsc();
}
