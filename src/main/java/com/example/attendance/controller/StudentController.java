package com.example.attendance.controller;

import com.example.attendance.annotation.OpLog;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class StudentController {

    private final StudentService studentService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public StudentController(StudentService studentService, CourseRepository courseRepository,
                             UserRepository userRepository) {
        this.studentService = studentService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/student/list")
    public String list(@RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String studentName,
                       @RequestParam(required = false) String courseId,
                       @RequestParam(required = false) String department,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "id") String sortField,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       Authentication authentication,
                       Model model) {

        // 获取当前用户
        User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 教师只能看自己的课程的学生
        List<Course> availableCourses;
        if (isAdmin) {
            availableCourses = courseRepository.findAll(Sort.by("courseId"));
        } else if (currentUser != null) {
            availableCourses = courseRepository.findByTeacherId(currentUser.getId());
            // 如果教师没有指定课程筛选，默认只看自己的第一门课
            if ((courseId == null || courseId.isBlank()) && !availableCourses.isEmpty()) {
                courseId = availableCourses.get(0).getCourseId();
            }
        } else {
            availableCourses = List.of();
        }

        // 查询所有学院
        List<String> departments = studentService.allDepartments();

        // 分页搜索
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (!List.of("id", "studentId", "studentName", "courseId", "selectTime", "department").contains(sortField))
            sortField = "id";
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(dir, sortField));
        Page<Student> studentPage = studentService.list(studentId, studentName, courseId, department, pageable);

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("totalElements", studentPage.getTotalElements());

        model.addAttribute("studentId", studentId == null ? "" : studentId);
        model.addAttribute("studentName", studentName == null ? "" : studentName);
        model.addAttribute("courseId", courseId == null ? "" : courseId);
        model.addAttribute("department", department == null ? "" : department);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("availableCourses", availableCourses);
        model.addAttribute("departments", departments);
        model.addAttribute("isAdmin", isAdmin);

        return "student-list";
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/student/add")
    public String addPage(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
        return "student-form";
    }

    @OpLog("新增学生")
    @PostMapping("/student/save")
    public String save(@Valid @ModelAttribute("student") Student student,
                       BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
            return "student-form";
        }
        studentService.save(student);
        return "redirect:/student/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/student/edit/{id}")
    public String editPage(@PathVariable Integer id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
        return "student-form";
    }

    @OpLog("编辑学生")
    @PostMapping("/student/update")
    public String update(@Valid @ModelAttribute("student") Student student,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
            return "student-form";
        }
        studentService.update(student);
        return "redirect:/student/list";
    }

    @OpLog("删除学生")
    @GetMapping("/student/delete/{id}")
    public String delete(@PathVariable Integer id) {
        studentService.deleteById(id);
        return "redirect:/student/list";
    }

    @OpLog("批量删除学生")
    @PostMapping("/student/batchDelete")
    public String batchDelete(@RequestParam(required = false) List<Integer> ids) {
        studentService.batchDelete(ids);
        return "redirect:/student/list";
    }
}
