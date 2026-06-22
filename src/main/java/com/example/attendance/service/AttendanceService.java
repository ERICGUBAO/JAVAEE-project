package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AttendanceService {

    void checkIn(String studentId, String studentName, String courseId, String ip);

    /** 通过签到会话签到（跳过时间窗口校验） */
    void checkInViaSession(String studentId, String studentName, String courseId, String ip);

    /** 检查学生今天是否已在某课程签到 */
    boolean alreadyCheckedIn(String studentId, String courseId);

    void checkOut(String studentId, String courseId, String ip);

    Page<Attendance> list(String studentId,
                          LocalDate startDate,
                          LocalDate endDate,
                          String status,
                          String courseId,
                          Pageable pageable);
}