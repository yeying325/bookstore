-- ============================================================
-- 网上书店：书籍评价表 reviews
-- 用途：存放用户在书籍详情页写下的评分和评价内容
-- 执行方式（在项目根目录下，先执行过 books.sql 再执行这个）：
--   mysql -uroot -p --default-character-set=utf8mb4 -e "source bookstore-backend/sql/reviews.sql"
-- 脚本可以重复执行：表不存在时创建，示例评价按 id 覆盖更新
-- 注意：示例评价用的是 id 1-4，重复执行会把这 4 条记录覆盖成示例内容，
--       如果数据库里已经有真实评价，就不要重复执行这个脚本了
-- ============================================================

CREATE DATABASE IF NOT EXISTS `bookstore`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `bookstore`;

CREATE TABLE IF NOT EXISTS `reviews` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `book_id`     bigint       NOT NULL                COMMENT '被评价的书籍ID，对应 books.id',
  `username`    varchar(50)  NOT NULL                COMMENT '评价人用户名',
  `rating`      tinyint      NOT NULL DEFAULT 5      COMMENT '评分：1-5 星',
  `content`     varchar(500) NOT NULL                COMMENT '评价内容',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '首次发表时间',
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  -- 同一个用户对同一本书只保留一条评价，重复提交变成修改
  UNIQUE KEY `uk_book_user` (`book_id`, `username`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_username` (`username`),
  -- 书被删除时，它下面的评价一起删掉（后端删除书籍时也会做一遍清理）
  CONSTRAINT `fk_reviews_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB
  AUTO_INCREMENT=1
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='书籍评价表';

-- 示例评价（对应 books 表里的 1 三体 / 2 活着 / 4 解忧杂货店）
INSERT INTO `reviews` (`id`, `book_id`, `username`, `rating`, `content`) VALUES
  (1, 1, 'tom',   5, '宏大的宇宙想象，读到“黑暗森林”那段完全停不下来。'),
  (2, 1, 'lily',  4, '设定非常震撼，人物描写稍显单薄，但依然值得一读。'),
  (3, 2, 'jerry', 5, '文字很朴素，看完很久都没缓过来，关于活着本身的力量。'),
  (4, 4, 'lily',  5, '温暖治愈，适合心情低落的时候看，读完很平静。')
AS new
ON DUPLICATE KEY UPDATE
  `book_id`  = new.`book_id`,
  `username` = new.`username`,
  `rating`   = new.`rating`,
  `content`  = new.`content`;
