-- 教师
INSERT IGNORE INTO `user` (username, password, real_name, role) VALUES
('t201','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','张教授','TEACHER'),
('t202','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','李副教授','TEACHER'),
('t203','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','王教授','TEACHER'),
('t204','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','赵讲师','TEACHER'),
('t205','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','刘教授','TEACHER');

-- 课程（用 REPLACE INTO 避开 class_name）
REPLACE INTO course (course_id, course_name, start_time, end_time) VALUES
('C201','软件工程','08:00:00','09:40:00'),
('C202','数据库原理','10:00:00','11:40:00'),
('C203','高等数学','13:00:00','14:40:00'),
('C204','大学物理','15:00:00','16:40:00'),
('C205','大学英语','17:00:00','18:40:00');

-- 学生（30人 423开头）
INSERT IGNORE INTO `user` (username, password, real_name, role) VALUES
('42310001','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','陈同学','STUDENT'),
('42310002','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','林同学','STUDENT'),
('42310003','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','黄同学','STUDENT'),
('42310004','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','吴同学','STUDENT'),
('42310005','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','周同学','STUDENT'),
('42310006','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','郑同学','STUDENT'),
('42310007','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','冯同学','STUDENT'),
('42310008','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','何同学','STUDENT'),
('42310009','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','吕同学','STUDENT'),
('42310010','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','施同学','STUDENT'),
('42310011','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','张同学','STUDENT'),
('42310012','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','孔同学','STUDENT'),
('42310013','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','曹同学','STUDENT'),
('42310014','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','严同学','STUDENT'),
('42310015','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','华同学','STUDENT'),
('42310016','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','金同学','STUDENT'),
('42310017','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','魏同学','STUDENT'),
('42310018','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','陶同学','STUDENT'),
('42310019','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','姜同学','STUDENT'),
('42310020','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','戚同学','STUDENT'),
('42310021','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','谢同学','STUDENT'),
('42310022','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','邹同学','STUDENT'),
('42310023','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','苏同学','STUDENT'),
('42310024','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','潘同学','STUDENT'),
('42310025','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','葛同学','STUDENT'),
('42310026','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','范同学','STUDENT'),
('42310027','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','彭同学','STUDENT'),
('42310028','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','鲁同学','STUDENT'),
('42310029','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','马同学','STUDENT'),
('42310030','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','方同学','STUDENT');

-- 选课
INSERT IGNORE INTO course_selection (student_id, student_name, course_id) VALUES
('42310001','陈同学','C201'),('42310001','陈同学','C202'),
('42310002','林同学','C201'),('42310002','林同学','C202'),
('42310003','黄同学','C201'),('42310003','黄同学','C202'),
('42310004','吴同学','C201'),('42310004','吴同学','C202'),
('42310005','周同学','C201'),('42310005','周同学','C202'),
('42310006','郑同学','C201'),('42310006','郑同学','C202'),
('42310007','冯同学','C201'),('42310007','冯同学','C202'),
('42310008','何同学','C201'),('42310008','何同学','C202'),
('42310009','吕同学','C201'),('42310009','吕同学','C202'),
('42310010','施同学','C201'),('42310010','施同学','C202'),
('42310011','张同学','C203'),('42310011','张同学','C202'),
('42310012','孔同学','C203'),('42310012','孔同学','C202'),
('42310013','曹同学','C203'),('42310013','曹同学','C202'),
('42310014','严同学','C203'),('42310014','严同学','C202'),
('42310015','华同学','C203'),('42310015','华同学','C202'),
('42310016','金同学','C203'),('42310016','金同学','C202'),
('42310017','魏同学','C203'),('42310017','魏同学','C202'),
('42310018','陶同学','C203'),('42310018','陶同学','C202'),
('42310019','姜同学','C203'),('42310019','姜同学','C202'),
('42310020','戚同学','C203'),('42310020','戚同学','C202'),
('42310021','谢同学','C204'),
('42310022','邹同学','C204'),
('42310023','苏同学','C204'),
('42310024','潘同学','C204'),
('42310025','葛同学','C204'),
('42310026','范同学','C205'),
('42310027','彭同学','C205'),
('42310028','鲁同学','C205'),
('42310029','马同学','C205'),
('42310030','方同学','C205');

SELECT 'Teachers:' '', COUNT(*) FROM `user` WHERE role='TEACHER';
SELECT 'Students:' '', COUNT(*) FROM `user` WHERE role='STUDENT';
SELECT 'Courses:' '', COUNT(*) FROM course;
SELECT 'Enrollments:' '', COUNT(*) FROM course_selection;
SELECT course_id, course_name, start_time, end_time FROM course ORDER BY start_time;
