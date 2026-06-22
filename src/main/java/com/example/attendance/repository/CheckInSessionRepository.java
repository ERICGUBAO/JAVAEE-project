package com.example.attendance.repository;

import com.example.attendance.entity.CheckInSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckInSessionRepository extends JpaRepository<CheckInSession, Long> {

    /** 教师查看自己创建的活跃会话 */
    List<CheckInSession> findByTeacherIdAndStatus(Long teacherId, String status);

    /** 根据课程ID列表查活跃会话 */
    List<CheckInSession> findByCourseIdInAndStatus(List<String> courseIds, String status);
}
