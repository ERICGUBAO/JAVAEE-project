package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer>,
        JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByStudentIdAndCourseIdAndAttendDate(String studentId, String courseId, LocalDate attendDate);
}