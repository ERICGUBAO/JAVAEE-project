package com.example.attendance.service.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public void insertTeacher(User user) {
        if (user == null) throw new RuntimeException("请求体不能为空");
        if (user.getUsername() == null || user.getUsername().trim().isEmpty())
            throw new RuntimeException("username不能为空");
        if (user.getPassword() == null || user.getPassword().trim().isEmpty())
            throw new RuntimeException("password不能为空");
        if (user.getRealName() == null || user.getRealName().trim().isEmpty())
            throw new RuntimeException("realName不能为空");

        // 固定教师角色
        user.setRole("TEACHER");

        // 防止重复用户名
        if (userDao.findByUsername(user.getUsername().trim()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        userDao.insert(user);
    }

    @Override
    public User findById(Long id) {
        if (id == null) throw new RuntimeException("id不能为空");
        return userDao.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty())
            throw new RuntimeException("username不能为空");
        return userDao.findByUsername(username.trim());
    }

    @Override
    public List<User> findAllTeachers() {
        return userDao.findAllTeachers();
    }

    @Override
    public void update(User user) {
        if (user == null) throw new RuntimeException("请求体不能为空");
        if (user.getId() == null) throw new RuntimeException("id不能为空");
        if (user.getPassword() == null || user.getPassword().trim().isEmpty())
            throw new RuntimeException("password不能为空");
        if (user.getRealName() == null || user.getRealName().trim().isEmpty())
            throw new RuntimeException("realName不能为空");
        if (user.getRole() == null || user.getRole().trim().isEmpty())
            throw new RuntimeException("role不能为空");

        userDao.update(user);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("id不能为空");
        userDao.deleteById(id);
    }
}