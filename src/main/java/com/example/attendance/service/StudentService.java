package com.example.attendance.service;

import com.example.attendance.entity.AttendanceUpdateRequest;
import com.example.attendance.entity.Student;

import java.util.List;

public interface StudentService {
    Student getStudentInfo(String studentId);

    List<Student> listStudents(String className, Integer page);

    String updateAttendance(AttendanceUpdateRequest body);
}