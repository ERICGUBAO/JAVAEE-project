package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import com.example.attendance.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    // 新增教师用户
    @PostMapping("/user/teacher")
    public Result<String> insertTeacher(@RequestBody User user) {
        try {
            userService.insertTeacher(user);
            return Result.success("新增教师成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 根据ID查询
    @GetMapping("/user/{id}")
    public Result<User> findById(@PathVariable Long id) {
        try {
            return Result.success(userService.findById(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 根据用户名查询（登录验证）
    @GetMapping("/user/by-username")
    public Result<User> findByUsername(@RequestParam String username) {
        try {
            return Result.success(userService.findByUsername(username));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 查询所有教师
    @GetMapping("/user/teachers")
    public Result<List<User>> findAllTeachers() {
        try {
            return Result.success(userService.findAllTeachers());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 更新用户
    @PutMapping("/user")
    public Result<String> update(@RequestBody User user) {
        try {
            userService.update(user);
            return Result.success("更新成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 删除用户
    @DeleteMapping("/user/{id}")
    public Result<String> deleteById(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}