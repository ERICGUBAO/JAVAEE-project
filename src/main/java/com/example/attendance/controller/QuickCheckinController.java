package com.example.attendance.controller;

import com.example.attendance.entity.CheckInSession;
import com.example.attendance.entity.User;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CheckInSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class QuickCheckinController {

    private final CheckInSessionService sessionService;
    private final AttendanceService attendanceService;
    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final PasswordEncoder passwordEncoder;

    // === 反代签：IP + sessionId → 最近一次签到时间 ===
    private final Map<String, Long> recentCheckins = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_MS = 10 * 60 * 1000; // 10分钟

    public QuickCheckinController(CheckInSessionService sessionService,
                                  AttendanceService attendanceService,
                                  UserRepository userRepo,
                                  StudentRepository studentRepo,
                                  PasswordEncoder passwordEncoder) {
        this.sessionService = sessionService;
        this.attendanceService = attendanceService;
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /** 扫码签到页面（无需登录，但也支持已登录用户） */
    @GetMapping("/qr/{code}")
    public String qrPage(@PathVariable String code, Model model, Authentication auth) {
        try {
            CheckInSession s = sessionService.findActiveByCode(code);
            model.addAttribute("sessionCode", code);
            model.addAttribute("courseName", s.getCourseName());
            model.addAttribute("courseId", s.getCourseId());
            model.addAttribute("teacherName", s.getTeacherName());
            model.addAttribute("expired", false);

            // 如果已登录，自动填写
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                User user = userRepo.findByUsername(username).orElse(null);
                if (user != null && "STUDENT".equals(user.getRole())) {
                    model.addAttribute("loggedIn", true);
                    model.addAttribute("autoStudentId", username);
                    model.addAttribute("autoStudentName", user.getRealName());

                    // 检查是否已签到
                    boolean alreadyCheckedIn = attendanceService.alreadyCheckedIn(username, s.getCourseId());
                    model.addAttribute("alreadyCheckedIn", alreadyCheckedIn);
                }
            }
        } catch (Exception e) {
            model.addAttribute("expired", true);
        }

        return "quick-checkin";
    }

    /** 执行签到 */
    @PostMapping("/qr/checkin")
    @ResponseBody
    public String qrCheckin(@RequestParam String code,
                            @RequestParam String studentId,
                            @RequestParam(required = false, defaultValue = "") String studentName,
                            HttpServletRequest request) {
        try {
            CheckInSession session = sessionService.findActiveByCode(code.trim());
            String ip = request.getRemoteAddr();

            // === 反代签：同IP+同会话 10分钟内只能签一次 ===
            String rateKey = ip + ":" + session.getId();
            Long lastTime = recentCheckins.get(rateKey);
            long now = System.currentTimeMillis();
            if (lastTime != null && (now - lastTime) < RATE_LIMIT_MS) {
                long waitSeconds = (RATE_LIMIT_MS - (now - lastTime)) / 1000;
                return "签到过于频繁，请" + waitSeconds + "秒后再试（同设备10分钟内限签一次）";
            }
            recentCheckins.put(rateKey, now);

            // 自动创建账号
            String sid = studentId.trim();
            User user = userRepo.findByUsername(sid).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(sid);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRealName(studentName.isBlank() ? sid : studentName);
                user.setRole("STUDENT");
                userRepo.save(user);
            }

            // 自动选课
            String courseId = session.getCourseId();
            boolean enrolled = studentRepo.findByCourseId(courseId).stream()
                    .anyMatch(s -> s.getStudentId().equals(sid));
            if (!enrolled) {
                com.example.attendance.entity.Student st = new com.example.attendance.entity.Student();
                st.setStudentId(sid);
                st.setStudentName(user.getRealName());
                st.setCourseId(courseId);
                studentRepo.save(st);
            }

            // 签到
            attendanceService.checkInViaSession(sid, user.getRealName(), courseId, ip);

            return "签到成功！" + user.getRealName() + " 已签到 " + session.getCourseName();

        } catch (Exception e) {
            return "签到失败：" + e.getMessage();
        }
    }
}
