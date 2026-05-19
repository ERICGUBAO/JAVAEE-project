package com.example.attendance.dao;

import com.example.attendance.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 新增教师用户
    public void insert(User user) {
        String sql = "INSERT INTO `user` (username, password, real_name, role) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getRole()
        );
    }

    // 根据ID查询
    public User findById(Long id) {
        String sql = "SELECT id, username, password, real_name AS realName, role FROM `user` WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 根据用户名查询（用于登录验证）
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, real_name AS realName, role FROM `user` WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 查询所有教师
    public List<User> findAllTeachers() {
        String sql = "SELECT id, username, password, real_name AS realName, role FROM `user` WHERE role = 'TEACHER'";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    // 更新用户
    public void update(User user) {
        String sql = "UPDATE `user` SET password = ?, real_name = ?, role = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getId()
        );
    }

    // 删除用户
    public void deleteById(Long id) {
        String sql = "DELETE FROM `user` WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}