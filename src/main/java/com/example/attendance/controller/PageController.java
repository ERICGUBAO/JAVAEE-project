package com.example.attendance.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        model.addAttribute("title", "系统首页");

        if (authentication == null) {
            model.addAttribute("username", "未登录");
            model.addAttribute("role", "");
            return "index";
        }

        model.addAttribute("username", authentication.getName());

        // 例如 Spring Security 里通常是 ROLE_TEACHER / ROLE_ADMIN / ROLE_STUDENT
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        // 显示成 TEACHER / ADMIN / STUDENT（去掉 ROLE_ 前缀）
        if (role.startsWith("ROLE_")) {
            role = role.substring("ROLE_".length());
        }

        model.addAttribute("role", role);
        return "index";
    }
}