package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
@Controller
public class StudentController {

    private final StudentService studentService;
    private final CourseRepository courseRepository;

    public StudentController(StudentService studentService, CourseRepository courseRepository) {
        this.studentService = studentService;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/student/list")
    public String list(@RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String studentName,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "id") String sortField,
                       @RequestParam(defaultValue = "desc") String sortDir,
                       Model model) {

        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (!List.of("id","studentId","studentName","courseId","selectTime").contains(sortField)) sortField = "id";

        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(dir, sortField));
        Page<Student> studentPage = studentService.list(studentId, studentName, pageable);

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("totalElements", studentPage.getTotalElements());

        model.addAttribute("studentId", studentId == null ? "" : studentId);
        model.addAttribute("studentName", studentName == null ? "" : studentName);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        return "student-list";
    }

    @GetMapping("/student/add")
    public String addPage(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
        return "student-form";
    }

    @PostMapping("/student/save")
    public String save(@Valid @ModelAttribute("student") Student student,
                       BindingResult br,
                       Model model) {
        if (br.hasErrors()) {
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
            return "student-form";
        }
        studentService.save(student);
        return "redirect:/student/list";
    }

    @GetMapping("/student/edit/{id}")
    public String editPage(@PathVariable Integer id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
        return "student-form";
    }

    @PostMapping("/student/update")
    public String update(@Valid @ModelAttribute("student") Student student,
                         BindingResult br,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
            return "student-form";
        }
        studentService.update(student);
        return "redirect:/student/list";
    }

    @GetMapping("/student/delete/{id}")
    public String delete(@PathVariable Integer id) {
        studentService.deleteById(id);
        return "redirect:/student/list";
    }

    @PostMapping("/student/batchDelete")
    public String batchDelete(@RequestParam(required = false) List<Integer> ids) {
        studentService.batchDelete(ids);
        return "redirect:/student/list";
    }
}