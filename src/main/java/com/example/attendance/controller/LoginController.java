package com.example.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String logout,
                            Model model) {

        model.addAttribute("title", "用户登录");

        if (error != null) {
            model.addAttribute("errorMsg", "用户名或密码错误");
        }
        if (registered != null) {
            model.addAttribute("successMsg", "注册成功，请登录");
        }
        if (logout != null) {
            model.addAttribute("successMsg", "已退出登录");
        }

        return "login"; // templates/login.html
    }
}