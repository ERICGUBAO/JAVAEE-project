package com.example.attendance.controller;

import com.example.attendance.entity.AttendanceUpdateRequest;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 任务一：路径参数
    @GetMapping("/student/info/{studentId}")
    public Result<Student> getStudentInfo(@PathVariable String studentId) {
        try {
            return Result.success(studentService.getStudentInfo(studentId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 任务二：查询参数
    @GetMapping("/student/list")
    public Result<List<Student>> listStudents(
            @RequestParam(required = false) String className,
            @RequestParam(defaultValue = "1") Integer page
    ) {
        try {
            return Result.success("page=" + page, studentService.listStudents(className, page));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 任务三：JSON体参数
    @PostMapping("/attendance/update")
    public Result<String> updateAttendance(@RequestBody AttendanceUpdateRequest body) {
        try {
            return Result.success("更新成功", studentService.updateAttendance(body));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}