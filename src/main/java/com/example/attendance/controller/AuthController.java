//package com.example.attendance.controller;
//
//import com.example.attendance.entity.LoginRequest;
//import com.example.attendance.entity.RegisterRequest;
//import com.example.attendance.entity.User;
//import com.example.attendance.repository.UserRepository;
//import com.example.attendance.util.Result;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//public class AuthController {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @PostMapping("/register")
//    public Result<String> register(@RequestBody RegisterRequest req) {
//        if (req == null) return Result.error("请求体不能为空");
//        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) return Result.error("username 不能为空");
//        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) return Result.error("password 不能为空");
//        if (req.getRole() == null || req.getRole().trim().isEmpty()) return Result.error("role 不能为空");
//        if (req.getRealName() == null || req.getRealName().trim().isEmpty()) return Result.error("realName 不能为空");
//
//        String username = req.getUsername().trim();
//        String rawPassword = req.getPassword().trim();
//        String role = req.getRole().trim().toUpperCase();
//        String realName = req.getRealName().trim();
//
//        if (userRepository.existsByUsername(username)) {
//            return Result.error("用户名已存在");
//        }
//
//        if (!role.equals("ADMIN") && !role.equals("TEACHER") && !role.equals("STUDENT")) {
//            return Result.error("role 只能是 ADMIN / TEACHER / STUDENT");
//        }
//
//        User u = new User();
//        u.setUsername(username);
//        u.setPassword(passwordEncoder.encode(rawPassword));
//        u.setRealName(realName);
//        u.setRole(role);
//
//        // 你的表里 status 是 NOT NULL 且默认 1：
//        // 如果你实体里没映射 status，这里可以不设置，让数据库默认值生效。
//        // 如果你后面把 status 字段加进实体，再在这里 setStatus((byte)1);
//
//        userRepository.save(u);
//        return Result.success("注册成功", "ok");
//    }
//
//    @PostMapping("/login")
//    public Result<String> login(@RequestBody LoginRequest req) {
//        if (req == null) return Result.error("请求体不能为空");
//        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) return Result.error("username 不能为空");
//        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) return Result.error("password 不能为空");
//
//        try {
//            Authentication auth = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
//            );
//            return Result.success("登录成功", auth.getName());
//        } catch (BadCredentialsException e) {
//            return Result.error("用户名或密码错误");
//        }
//    }
//}