package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Page<Student> list(String studentId, String studentName, Pageable pageable) {
        String sid = studentId == null ? "" : studentId.trim();
        String sname = studentName == null ? "" : studentName.trim();
        return studentRepository.findByStudentIdContainingAndStudentNameContaining(sid, sname, pageable);
    }

    @Override
    public Student findById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到记录 id=" + id));
    }

    @Override
    public void save(Student student) {
        // 新增时补一个时间（可选）
        if (student.getSelectTime() == null) {
            student.setSelectTime(LocalDateTime.now());
        }
        studentRepository.save(student);
    }

    @Override
    public void update(Student student) {
        if (student.getId() == null) throw new RuntimeException("更新失败：id 不能为空");
        if (!studentRepository.existsById(student.getId()))
            throw new RuntimeException("更新失败：记录不存在 id=" + student.getId());

        // 不强制覆盖 selectTime（如果你想保留原值，就不动）
        studentRepository.save(student);
    }

    @Override
    public void deleteById(Integer id) {
        studentRepository.deleteById(id);
    }

    @Override
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        studentRepository.deleteAllById(ids);
    }
}