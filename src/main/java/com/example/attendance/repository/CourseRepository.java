package com.example.attendance.repository;

import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
    /** 查某教师的所有课程 */
    List<Course> findByTeacherId(Long teacherId);
}