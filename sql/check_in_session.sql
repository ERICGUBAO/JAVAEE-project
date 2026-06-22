-- 签到会话表
-- 运行此 SQL 创建新表，然后重启应用

CREATE TABLE IF NOT EXISTS `check_in_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `course_id` VARCHAR(20) NOT NULL,
    `course_name` VARCHAR(100) NOT NULL,
    `teacher_id` BIGINT NOT NULL,
    `teacher_name` VARCHAR(50) NOT NULL,
    `code` VARCHAR(4) NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `status` VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (`id`),
    INDEX `idx_code` (`code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
