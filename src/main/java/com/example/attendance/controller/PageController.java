package com.example.attendance.controller;

import com.example.attendance.entity.CheckInSession;
import com.example.attendance.entity.User;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.CheckInSessionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class PageController {

    private final CheckInSessionService sessionService;
    private final UserRepository userRepository;

    public PageController(CheckInSessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        model.addAttribute("title", "系统首页");

        if (authentication == null) {
            model.addAttribute("username", "未登录");
            model.addAttribute("role", "");
            return "index";
        }

        model.addAttribute("username", authentication.getName());

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        if (role.startsWith("ROLE_")) {
            role = role.substring("ROLE_".length());
        }

        model.addAttribute("role", role);

        // === 签到会话数据 ===
        User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (currentUser != null) {
            if ("STUDENT".equals(role)) {
                List<CheckInSession> sessions = sessionService.findActiveForStudent(currentUser.getUsername());
                model.addAttribute("activeSessions", sessions);
                model.addAttribute("myActiveSession", null);
            } else if ("TEACHER".equals(role) || "ADMIN".equals(role)) {
                CheckInSession mySession = sessionService.findTeacherActiveSession(currentUser.getId());
                // 断线重连：如果有活跃会话，直接跳转面板
                if (mySession != null) {
                    return "redirect:/attendance/session/panel/" + mySession.getId();
                }
                model.addAttribute("myActiveSession", null);
                model.addAttribute("activeSessions", java.util.Collections.emptyList());
            }
        } else {
            model.addAttribute("activeSessions", java.util.Collections.emptyList());
            model.addAttribute("myActiveSession", null);
        }

        return "index";
    }
}