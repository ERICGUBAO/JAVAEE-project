package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterPageController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterPageController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // templates/register.html
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             @RequestParam String realName,
                             @RequestParam String role,
                             Model model) {

        // 失败时回显（避免用户重填）
        model.addAttribute("username", username);
        model.addAttribute("realName", realName);
        model.addAttribute("role", role);

        username = username == null ? "" : username.trim();
        realName = realName == null ? "" : realName.trim();
        role = role == null ? "" : role.trim().toUpperCase();

        if (username.isEmpty()) {
            model.addAttribute("errorMsg", "用户名不能为空");
            return "register";
        }
        if (password == null || password.isEmpty()) {
            model.addAttribute("errorMsg", "密码不能为空");
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "两次密码不一致");
            return "register";
        }
        if (realName.isEmpty()) {
            model.addAttribute("errorMsg", "真实姓名不能为空");
            return "register";
        }
        if (!role.equals("ADMIN") && !role.equals("TEACHER") && !role.equals("STUDENT")) {
            model.addAttribute("errorMsg", "角色只能是 ADMIN / TEACHER / STUDENT");
            return "register";
        }
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("errorMsg", "用户名已存在，请换一个");
            return "register";
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setRealName(realName);
        u.setRole(role);
        userRepository.save(u);

        // 成功：跳转登录页并提示
        return "redirect:/login?registered=true";
    }
}