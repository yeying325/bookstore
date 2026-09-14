package com.bookstore.bookstorebackend.repository;

import com.bookstore.bookstorebackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 某本书的全部评价，最新的排在前面（详情页用） */
    List<Review> findByBookIdOrderByIdDesc(Long bookId);

    /** 某个用户在某本书下写过的评价（用来判断是新增还是修改） */
    Review findFirstByBookIdAndUsername(Long bookId, String username);

    /** 全部评价，最新的排在前面（管理员评价管理用） */
    List<Review> findAllByOrderByIdDesc();

    /** 书籍被删除时，把它下面的评价一起删掉，不留下孤儿评价 */
    void deleteByBookId(Long bookId);
}
