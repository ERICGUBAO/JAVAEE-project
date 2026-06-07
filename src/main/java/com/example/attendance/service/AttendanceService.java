package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AttendanceService {

    void checkIn(String studentId, String studentName, String courseId, String ip);

    void checkOut(String studentId, String courseId, String ip);

    Page<Attendance> list(String studentId,
                          LocalDate startDate,
                          LocalDate endDate,
                          String status,
                          String courseId,
                          Pageable pageable);
}