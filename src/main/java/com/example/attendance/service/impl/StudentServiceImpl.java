package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<Student> list(String studentId, String studentName, String courseId, String department, Pageable pageable) {
        String sid = studentId == null ? "" : studentId.trim();
        String sname = studentName == null ? "" : studentName.trim();
        String cid = courseId == null ? "" : courseId.trim();
        String dep = department == null ? "" : department.trim();
        return studentRepository.findByStudentIdContainingAndStudentNameContainingAndCourseIdContainingAndDepartmentContaining(sid, sname, cid, dep, pageable);
    }

    @Override
    public List<String> allDepartments() {
        return studentRepository.findAllDepartments();
    }

    @Override
    public Student findById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到记录 id=" + id));
    }

    @Override
    @Transactional
    public void save(Student student) {
        if (student.getSelectTime() == null) {
            student.setSelectTime(LocalDateTime.now());
        }
        // 自动创建用户账号（用户名=学号，密码=123456）
        String sid = student.getStudentId();
        if (sid != null && !sid.isBlank() && userRepository.findByUsername(sid).isEmpty()) {
            User u = new User();
            u.setUsername(sid);
            u.setPassword(passwordEncoder.encode("123456"));
            u.setRealName(student.getStudentName() != null ? student.getStudentName() : sid);
            u.setRole("STUDENT");
            userRepository.save(u);
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