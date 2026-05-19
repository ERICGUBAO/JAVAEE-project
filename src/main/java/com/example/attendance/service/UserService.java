package com.example.attendance.service;

import com.example.attendance.entity.User;

import java.util.List;

public interface UserService {

    // 新增教师
    void insertTeacher(User user);

    // 根据ID查询
    User findById(Long id);

    // 根据用户名查询（登录验证）
    User findByUsername(String username);

    // 查询所有教师
    List<User> findAllTeachers();

    // 更新用户
    void update(User user);

    // 删除用户
    void deleteById(Long id);
}