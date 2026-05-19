package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    // 搜索：按学号/姓名模糊匹配（分页 + 排序由 Pageable 决定）
    Page<Student> findByStudentIdContainingAndStudentNameContaining(
            String studentId,
            String studentName,
            Pageable pageable
    );
}