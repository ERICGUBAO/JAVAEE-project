package com.example.attendance.controller;

import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.service.StudentService;
import com.example.attendance.util.Result;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RESTful API —— 返回 JSON 而不是 HTML 页面
 *
 * 和现有 Controller 的区别：
 *   @Controller     → 返回 HTML 页面（Thymeleaf 渲染）
 *   @RestController → 返回 JSON 数据（给前端/APP/其他系统调用）
 *
 * 面试话术：
 * "项目中既有 @Controller 做服务端渲染页面，也有 @RestController 提供 RESTful API，
 * API 返回统一的 Result 包装格式，包含 success、message、data 三个字段。"
 */
@RestController
@RequestMapping("/api")
public class CourseApiController {

    private final CourseRepository courseRepository;
    private final StudentService studentService;

    public CourseApiController(CourseRepository courseRepository,
                               StudentService studentService) {
        this.courseRepository = courseRepository;
        this.studentService = studentService;
    }

    // ==================== 缓存演示 ====================

    /**
     * 查询所有课程 —— 带缓存
     *
     * 原理：
     *   第一次访问 → 查数据库 → 存到缓存 → 返回结果
     *   第二次访问 → 直接从缓存拿 → 不查数据库（快 100 倍+）
     *
     * @Cacheable 的工作流程：
     *   1. 先查缓存，key = "courses::all"
     *   2. 有 → 直接返回，不执行方法体
     *   3. 没有 → 执行方法体（查数据库）→ 结果存 Redis → 返回
     */
    @Cacheable(value = "courses", key = "'all'")
    @GetMapping("/courses")
    public Result<List<Course>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return Result.success(courses);
    }

    /**
     * 新增课程 —— 同时清除缓存
     *
     * 原理：
     *   新增了课程，缓存里的旧课程列表就过时了。
     *   @CacheEvict 强制清除缓存里的 "courses::all"，下次查询会重新查库。
     *
     *   allEntries = true 表示清掉 courses 这个命名空间下的所有缓存
     */
    @CacheEvict(value = "courses", allEntries = true)
    @PostMapping("/courses")
    public Result<String> addCourse(@RequestBody Course course) {
        courseRepository.save(course);
        return Result.success("课程添加成功");
    }

    // ==================== Result 演示 ====================

    /**
     * 根据 ID 查学生 —— 演示 Result 包装
     *
     * 成功时返回:  {"success": true,  "message": "success", "data": {...}}
     * 失败时返回:  {"success": false, "message": "未找到记录", "data": null}
     * （失败的情况由 GlobalExceptionHandler 自动处理）
     */
    @GetMapping("/students/{id}")
    public Result<Student> getStudent(@PathVariable Integer id) {
        Student student = studentService.findById(id);
        return Result.success(student);
    }
}
