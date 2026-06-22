package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public PasswordController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/change-password")
    public String page() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String change(@RequestParam String oldPassword,
                         @RequestParam String newPassword,
                         @RequestParam String confirmPassword,
                         Authentication auth,
                         RedirectAttributes ra) {
        User user = userRepo.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "用户不存在");
            return "redirect:/change-password";
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "原密码错误");
            return "redirect:/change-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "两次新密码不一致");
            return "redirect:/change-password";
        }
        if (newPassword.length() < 4) {
            ra.addFlashAttribute("error", "新密码至少4位");
            return "redirect:/change-password";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        ra.addFlashAttribute("success", "密码修改成功");
        return "redirect:/change-password";
    }
}
