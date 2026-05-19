package com.example.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 静态资源
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // Thymeleaf 页面路由放行
                        .requestMatchers("/login", "/register").permitAll()
                        // 其他请求需要登录
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")              // GET /login 由你自己的 LoginController 返回模板
                        .loginProcessingUrl("/login")     // POST /login 由 Spring Security 处理
                        .defaultSuccessUrl("/", true)     // 登录成功跳首页（GET /）
                        .failureUrl("/login?error=true")  // 登录失败提示
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                );

        return http.build();
    }
}