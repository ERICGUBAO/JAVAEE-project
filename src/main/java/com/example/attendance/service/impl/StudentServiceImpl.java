package com.example.attendance.service.impl;

import com.example.attendance.entity.AttendanceUpdateRequest;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Override
    public Student getStudentInfo(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new RuntimeException("studentId 不能为空");
        }

        // Step5：先��假数据，Step6/7/8 再接 Dao + 数据库
        Student s = new Student();
        s.setStudentId(studentId);
        s.setName("张三");
        s.setClassName("数据可视化2026春");
        return s;
    }

    @Override
    public List<Student> listStudents(String className, Integer page) {
        int p = (page == null || page < 1) ? 1 : page;

        // 这里按课件：className 是筛选条件（可选）
        String finalClassName = (className == null || className.trim().isEmpty())
                ? "数据可视化2026春"
                : className.trim();

        // Step5：假数据
        List<Student> list = new ArrayList<>();

        Student s1 = new Student();
        s1.setStudentId("2023001");
        s1.setName("张三");
        s1.setClassName(finalClassName);
        list.add(s1);

        Student s2 = new Student();
        s2.setStudentId("2023002");
        s2.setName("李四");
        s2.setClassName(finalClassName);
        list.add(s2);

        // 这里只演示 page 参数接收成功，不做真实分页
        return list;
    }

    @Override
    public String updateAttendance(AttendanceUpdateRequest body) {
        if (body == null) throw new RuntimeException("请求体不能为空");
        if (body.getStudentId() == null || body.getStudentId().trim().isEmpty())
            throw new RuntimeException("studentId 不能为空");
        if (body.getDate() == null || body.getDate().trim().isEmpty())
            throw new RuntimeException("date 不能为空");
        if (body.getStatus() == null || body.getStatus().trim().isEmpty())
            throw new RuntimeException("status 不能为空");

        return "学号 " + body.getStudentId().trim() + " 在 " + body.getDate().trim()
                + " 考勤更新为 " + body.getStatus().trim();
    }
}