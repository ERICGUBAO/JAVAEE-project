// src/main/java/com/example/attendance/repository/AttendanceRepository.java
package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByCourseId(String courseId);

    List<Attendance> findByStudentId(String studentId);

    List<Attendance> findByCourseIdAndStudentId(String courseId, String studentId);
}