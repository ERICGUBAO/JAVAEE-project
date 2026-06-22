package com.example.attendance.service;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {

    Page<Student> list(String studentId, String studentName, String courseId, String department, Pageable pageable);

    List<String> allDepartments();

    Student findById(Integer id);

    void save(Student student);

    void update(Student student);

    void deleteById(Integer id);

    void batchDelete(List<Integer> ids);
}