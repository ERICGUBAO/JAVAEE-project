package com.example.attendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class StudentController {

    @GetMapping("/student/info")
    public Map<String, Object> getStudentInfo() {
        Map<String, Object> student = new LinkedHashMap<>();
        student.put("name", "张三");
        student.put("studentId", "2023001");
        student.put("className", "数据可视化2026春");
        return student;
    }

    @PostMapping("/student/attendance")
    public Map<String, Object> submitAttendance(@RequestBody Map<String, Object> body) {
        Object studentId = body.get("studentId");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "学号为 " + studentId + " 的学生打卡成功！");
        return result;
    }

    @GetMapping("/student/courses")
    public List<String> getCourses() {
        return Arrays.asList("高等数学", "Java程序设计", "数据可视化");
    }
}