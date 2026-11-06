-- ============================================================
-- 宠物领养管理系统 · 一键初始化脚本（表结构 + 演示数据）
-- 用法：mysql -uroot -p < db/schema_full.sql
--       或用 Navicat / DataGrip 打开本文件整体执行
-- 内容：自动创建 pet_adopt 库、7 张表、管理员 admin/123456，
--       并导入演示数据（用户/宠物/申请/记录/回访/公告）
-- 警告：脚本含 DROP TABLE IF EXISTS，只用于空库或新电脑；
--       在已有业务数据的库上执行会清空原有数据。
-- ============================================================

-- ============================================================
-- 宠物领养管理系统 数据库建表脚本
-- 字符集: utf8mb4    引擎: InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS `pet_adopt`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `pet_adopt`;

-- ------------------------------------------------------------
-- 1. 管理员表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt 加密存储）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='管理员表';

-- ------------------------------------------------------------
-- 2. 宠物档案表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '宠物名字',
    `type`        VARCHAR(20)  NOT NULL COMMENT '宠物类型：狗狗/猫咪/兔子/其他',
    `breed`       VARCHAR(50)  DEFAULT NULL COMMENT '品种',
    `age`         INT          DEFAULT NULL COMMENT '年龄（月）',
    `gender`      VARCHAR(10)  DEFAULT NULL COMMENT '性别：公/母',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '宠物照片地址',
    `health`      VARCHAR(255) DEFAULT NULL COMMENT '健康状况',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1可领养 2已领养 3救助中',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_pet_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='宠物档案表';

-- ------------------------------------------------------------
-- 3. 领养申请表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `adopt_apply`;
CREATE TABLE `adopt_apply` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        BIGINT       DEFAULT NULL COMMENT '提交申请的用户id',
    `apply_no`       VARCHAR(32)  NOT NULL COMMENT '申请编号',
    `applicant_name` VARCHAR(50)  NOT NULL COMMENT '申请人姓名',
    `phone`          VARCHAR(20)  NOT NULL COMMENT '联系电话',
    `pet_id`         BIGINT       NOT NULL COMMENT '宠物ID',
    `pet_name`       VARCHAR(50)  DEFAULT NULL COMMENT '宠物名字',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1通过 2驳回',
    `apply_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `reason`         VARCHAR(500) DEFAULT NULL COMMENT '领养理由',
    `reject_reason`  VARCHAR(200) DEFAULT NULL COMMENT '驳回原因',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_apply_no` (`apply_no`),
    KEY `idx_apply_pet_id` (`pet_id`),
    KEY `idx_apply_status` (`status`),
    KEY `idx_apply_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='领养申请表';

-- ------------------------------------------------------------
-- 4. 领养记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `adopt_record`;
CREATE TABLE `adopt_record` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `pet_id`        BIGINT       NOT NULL COMMENT '宠物ID',
    `pet_name`      VARCHAR(50)  DEFAULT NULL COMMENT '宠物名字',
    `adopter_name`  VARCHAR(50)  NOT NULL COMMENT '领养人姓名',
    `adopter_phone` VARCHAR(20)  DEFAULT NULL COMMENT '领养人电话',
    `adopt_date`    DATE         DEFAULT NULL COMMENT '领养日期',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_record_pet_id` (`pet_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='领养记录表';

-- ------------------------------------------------------------
-- 5. 回访表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `return_visit`;
CREATE TABLE `return_visit` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `pet_name`      VARCHAR(50)  DEFAULT NULL COMMENT '宠物名字',
    `adopter_name`  VARCHAR(50)  DEFAULT NULL COMMENT '领养人姓名',
    `plan_time`     DATE         DEFAULT NULL COMMENT '计划回访日期',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0待回访 1已完成',
    `content`       VARCHAR(500) DEFAULT NULL COMMENT '回访内容',
    PRIMARY KEY (`id`),
    KEY `idx_visit_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='回访表';

-- ------------------------------------------------------------
-- 6. 普通用户表（用户端账号，与管理员账号 sys_user 相互独立）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt 加密存储）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `address`     VARCHAR(255) DEFAULT NULL COMMENT '联系地址',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='普通用户表';

-- ------------------------------------------------------------
-- 7. 公告表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(100) NOT NULL COMMENT '公告标题',
    `content`     TEXT         COMMENT '公告内容',
    `is_top`      TINYINT      DEFAULT 0 COMMENT '是否置顶 0否/1是',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='公告表';

-- ------------------------------------------------------------
-- 初始化数据：预置管理员账号
--   用户名：admin
--   密码  ：123456
--   说明  ：password 存的是 Hutool BCrypt 加密值
--           （BCrypt.hashpw("123456") 生成，
--            BCrypt.checkpw("123456", 该值) 校验结果为 true）。
--   执行方式：
--           1) 命令行：mysql -uroot -p < db/schema.sql
--           2) 图形工具：Navicat / DataGrip / SQLyog 打开本文件直接运行
--              （脚本首行会自动创建 pet_adopt 库并切换）
--           3) 若库表已存在、只想补管理员数据，单独执行下面的 INSERT 即可。
-- ------------------------------------------------------------
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `avatar`)
SELECT 'admin',
       '$2a$10$CBhUDdrL1wVSf2YxZjo1reZ0giGtj9ROHWPTrCPKbz5YOqIbctRCa',
       '系统管理员',
       'https://avatars.githubusercontent.com/u/1?v=4'
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');

/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 ALTER TABLE `adopt_apply` DISABLE KEYS */;
INSERT INTO `adopt_apply` (`id`, `user_id`, `apply_no`, `applicant_name`, `phone`, `pet_id`, `pet_name`, `status`, `apply_time`, `reason`, `reject_reason`) VALUES (5,NULL,'AP202611041022319268','张三','13800000000',3,'旺财',1,'2026-11-04 10:22:31','喜欢狗狗，有养狗经验',NULL),(6,NULL,'AP202611041030089897','张三','13800000000',6,'旺财',2,'2026-11-04 10:30:08','喜欢狗狗，有养狗经验','居住环境不满足要求'),(11,3,'AP202611051448154791','小李','13800000001',12,'兔子2号',0,'2026-11-05 14:48:16','有养宠经验，家里有独立阳台',NULL),(13,6,'AP202611051501089802','??','13800138000',11,'1',0,'2026-11-05 15:01:09','????????,?????????,??????',NULL),(14,7,'AP202611051503284645','领养测试官','13900139000',13,'3',0,'2026-11-05 15:03:28','我有一份稳定的工作和独立的居所，想给它一个温暖安全的家，会认真陪伴和照顾它。',NULL),(15,8,'AP202611051506349976','领养测试官','13900139000',13,'3',0,'2026-11-05 15:06:34','我有一份稳定的工作和独立的居所，想给它一个温暖安全的家，会认真陪伴和照顾它。',NULL),(16,10,'AP202611051521088449','用户1','13447558544',13,'3',1,'2026-11-05 15:21:08','我要我要啊哒哒哒伟大的',NULL),(17,13,'AP202611051536506743','联调甲','13900139000',16,'联调喵64209900',1,'2026-11-05 15:36:51','我有一份稳定工作和独立居所，会认真陪伴照顾它，希望给它一个温暖的家。',NULL),(18,14,'AP202611051536505495','联调乙','13900139001',16,'联调喵64209900',2,'2026-11-05 15:36:51','我也非常想要这只小猫咪，家里条件很好，愿意用心照顾。','等'),(19,15,'AP202611051541033381','联调甲','13900139000',17,'联调喵64460920',1,'2026-11-05 15:41:03','我有一份稳定工作和独立居所，会认真陪伴照顾它，希望给它一个温暖的家。',NULL),(20,17,'AP202611051541119635','联调甲','13900139000',18,'联调喵64470954',1,'2026-11-05 15:41:12','我有一份稳定工作和独立居所，会认真陪伴照顾它，希望给它一个温暖的家。',NULL),(21,19,'AP202611051543113628','审核甲','13900139000',20,'审核宠590870',1,'2026-11-05 15:43:12','条件稳定，有独立居所，愿意长期负责这只小家伙。',NULL),(22,20,'AP202611051543562130','审核甲','13900139000',21,'审核宠635964',1,'2026-11-05 15:43:56','条件稳定，有独立居所，愿意长期负责这只小家伙。',NULL);
/*!40000 ALTER TABLE `adopt_apply` ENABLE KEYS */;

/*!40000 ALTER TABLE `adopt_record` DISABLE KEYS */;
INSERT INTO `adopt_record` (`id`, `pet_id`, `pet_name`, `adopter_name`, `adopter_phone`, `adopt_date`, `create_time`) VALUES (6,3,'旺财','张三','13800000000','2026-11-04','2026-11-04 10:27:04'),(8,13,'3','用户1','13447558544','2026-11-05','2026-11-05 15:22:18'),(9,16,'联调喵64209900','联调甲','13900139000','2026-11-05','2026-11-05 15:36:51'),(10,17,'联调喵64460920','联调甲','13900139000','2026-11-05','2026-11-05 15:41:04'),(11,18,'联调喵64470954','联调甲','13900139000','2026-11-05','2026-11-05 15:41:12'),(13,21,'审核宠635964','审核甲','13900139000','2026-11-05','2026-11-05 15:44:00'),(19,20,'审核宠590870','审核甲','13900139000','2026-11-05','2026-11-05 17:24:20');
/*!40000 ALTER TABLE `adopt_record` ENABLE KEYS */;

/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` (`id`, `title`, `content`, `is_top`, `create_time`) VALUES (4,'领养开放日公告（时间更新）','本周六将在中心举办宠物领养开放日活动，欢迎爱心人士前来参观领养。',1,'2026-11-04 10:56:40');
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;

/*!40000 ALTER TABLE `pet` DISABLE KEYS */;
INSERT INTO `pet` (`id`, `name`, `type`, `breed`, `age`, `gender`, `avatar`, `health`, `status`, `create_time`) VALUES (3,'旺财','狗狗','金毛',8,'公',NULL,'健康',2,'2026-11-04 09:59:21'),(6,'66','狗狗','金毛',9,'公','','健康',1,'2026-11-04 10:28:12'),(11,'1','猫咪','1',2,'母','','健康',1,'2026-11-04 17:03:37'),(12,'2','兔子','2',2,'母','','健康',1,'2026-11-04 17:20:55'),(13,'3','其他','3',3,'公','','已驱虫',2,'2026-11-04 17:21:45'),(20,'审核宠590870','狗狗','测试犬',6,'公',NULL,'健康',2,'2026-11-05 15:43:11');
/*!40000 ALTER TABLE `pet` ENABLE KEYS */;

/*!40000 ALTER TABLE `return_visit` DISABLE KEYS */;
INSERT INTO `return_visit` (`id`, `pet_name`, `adopter_name`, `plan_time`, `status`, `content`) VALUES (8,'旺财','张三','2026-11-10',1,'好'),(9,'旺财','张三','2026-11-10',1,'回访完成，旺财状态良好，领养人反馈满意'),(10,'旺财','张三','2026-11-10',1,'回访完成，旺财状态良好，领养人反馈满意'),(15,'6','6','2026-11-06',1,'都'),(19,'联调喵','联调甲','2026-11-20',1,'回访完成，宠物健康');
/*!40000 ALTER TABLE `return_visit` ENABLE KEYS */;

/*!40000 ALTER TABLE `user_info` DISABLE KEYS */;
INSERT INTO `user_info` (`id`, `username`, `password`, `nickname`, `phone`, `address`, `create_time`) VALUES (6,'testuser_3581','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','??','13800138000',NULL,'2026-11-05 15:01:02'),(7,'codex_62202046','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','改名后的官','13900139000',NULL,'2026-11-05 15:03:24'),(8,'codex_62386815','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','改名后的官','13900139000',NULL,'2026-11-05 15:06:29'),(9,'codex_filter_502165','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','codex_filter_502165','13900139000',NULL,'2026-11-05 15:08:23'),(10,'user1','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','用户1','13447558544',NULL,'2026-11-05 15:19:30'),(11,'itest_a64196745','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','联调甲','13900139000',NULL,'2026-11-05 15:36:37'),(12,'itest_b64196745','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','联调乙','13900139001',NULL,'2026-11-05 15:36:37'),(13,'itest_a64209900','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','联调甲','13900139000',NULL,'2026-11-05 15:36:50'),(14,'itest_b64209900','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','联调乙','13900139001',NULL,'2026-11-05 15:36:50'),(15,'itest_a64460920','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','联调甲','13900139000',NULL,'2026-11-05 15:41:03'),(16,'itest_b64460920','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','联调乙','13900139001',NULL,'2026-11-05 15:41:03'),(17,'itest_a64470954','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','联调甲','13900139000',NULL,'2026-11-05 15:41:11'),(18,'itest_b64470954','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','联调乙','13900139001',NULL,'2026-11-05 15:41:11'),(19,'uitest_590870','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','审核甲','13900139000',NULL,'2026-11-05 15:43:11'),(20,'uitest_635964','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','审核甲','13900139000',NULL,'2026-11-05 15:43:56'),(21,'sim_895673','$2a$10$EAV0HUPCgEyEoUxgSEt./uEqBevKAFaPqlO5JL3aIKjAqUjsLhqe.','sim_895673','13900139000',NULL,'2026-11-05 15:48:16');
/*!40000 ALTER TABLE `user_info` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

