-- ============================================================
-- 网上书店：书籍信息表 books
-- 用途：存放书名、作者、价格、简介、标签等信息，供后端 JPA 调用
-- 执行方式（在项目根目录下）：
--   mysql -uroot -p --default-character-set=utf8mb4 -e "source bookstore-backend/sql/books.sql"
-- 脚本可以重复执行：表不存在时创建，示例数据按 id 覆盖更新
-- ============================================================

CREATE DATABASE IF NOT EXISTS `bookstore`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `bookstore`;

CREATE TABLE IF NOT EXISTS `books` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
  `title`        varchar(100) NOT NULL                COMMENT '书名',
  `author`       varchar(100) NOT NULL                COMMENT '作者',
  `price`        decimal(10,2) NOT NULL DEFAULT 0.00  COMMENT '价格（元）',
  `intro`        varchar(500) DEFAULT NULL            COMMENT '简介',
  `tags`         varchar(200) DEFAULT NULL            COMMENT '标签，多个标签用英文逗号分隔',
  `cover_color`  varchar(20)  DEFAULT '#5e81ac'       COMMENT '封面背景色（前端卡片封面用）',
  `is_recommend` tinyint(1)   NOT NULL DEFAULT 0      COMMENT '是否推荐：1=在“推荐”页展示',
  `is_hot`       tinyint(1)   NOT NULL DEFAULT 0      COMMENT '是否热门：1=在“热门”页展示',
  `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_title` (`title`)
) ENGINE=InnoDB
  AUTO_INCREMENT=1
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='书籍信息表';

-- 示例数据：与前端原来的展示内容保持一致
-- 首页展示全部书籍；“推荐”看 is_recommend，“热门”看 is_hot
INSERT INTO `books`
  (`id`, `title`, `author`, `price`, `intro`, `tags`, `cover_color`, `is_recommend`, `is_hot`)
VALUES
  (1, '三体', '刘慈欣', 38.00, '中国科幻文学里程碑之作。', '科幻,中国文学', '#5e81ac', 1, 1),
  (2, '活着', '余华', 25.00, '讲述了人是为了活着本身而活着的。', '文学,人生', '#a3be8c', 1, 0),
  (3, '百年孤独', '马尔克斯', 55.00, '魔幻现实主义文学的代表作。', '文学,经典', '#ebcb8b', 0, 1),
  (4, '解忧杂货店', '东野圭吾', 42.00, '现代人内心流失的东西，这家杂货店能帮你找回。', '治愈,推理', '#bf616a', 1, 1),
  (5, '你当像鸟飞往你的山', '塔拉·韦斯特弗', 49.00, '教育意味着获得不同的视角，理解不同的人。', '成长,传记', '#b48ead', 0, 0)
AS new
ON DUPLICATE KEY UPDATE
  `title`        = new.`title`,
  `author`       = new.`author`,
  `price`        = new.`price`,
  `intro`        = new.`intro`,
  `tags`         = new.`tags`,
  `cover_color`  = new.`cover_color`,
  `is_recommend` = new.`is_recommend`,
  `is_hot`       = new.`is_hot`;
