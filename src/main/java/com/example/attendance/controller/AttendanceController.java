package com.example.attendance.controller;

import com.example.attendance.dto.CheckInProgress;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.CheckInSession;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.User;
import com.example.attendance.repository.CheckInSessionRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.AttendanceImportService;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CheckInSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceImportService attendanceImportService;
    private final CheckInSessionService sessionService;
    private final CheckInSessionRepository sessionRepo;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    public AttendanceController(AttendanceService attendanceService,
                                AttendanceImportService attendanceImportService,
                                CheckInSessionService sessionService,
                                CheckInSessionRepository sessionRepo,
                                CourseRepository courseRepository,
                                StudentRepository studentRepository,
                                UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.attendanceImportService = attendanceImportService;
        this.sessionService = sessionService;
        this.sessionRepo = sessionRepo;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // 学生：签到 / 签退
    // =========================

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/checkIn")
    public String checkInPage(Model model) {
        List<Course> courses = courseRepository.findAll(Sort.by("courseId"));
        model.addAttribute("courses", courses);
        return "attendance-check-in";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/checkIn")
    public String checkIn(@RequestParam String courseId,
                          @RequestParam(required = false) String remark, // 目前不入库
                          Authentication authentication,
                          HttpServletRequest request) {
        User user = currentUser(authentication);
        String ip = request.getRemoteAddr();
        try {
            attendanceService.checkIn(user.getUsername(), user.getRealName(), courseId, ip);
            return "redirect:/attendance/checkIn?success=1";
        } catch (RuntimeException ex) {
            return "redirect:/attendance/checkIn?error=" + url(ex.getMessage());
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/checkOut")
    public String checkOut(@RequestParam String courseId,
                           Authentication authentication,
                           HttpServletRequest request) {
        User user = currentUser(authentication);
        String ip = request.getRemoteAddr();
        try {
            attendanceService.checkOut(user.getUsername(), courseId, ip);
            return "redirect:/attendance/checkIn?success=1";
        } catch (RuntimeException ex) {
            return "redirect:/attendance/checkIn?error=" + url(ex.getMessage());
        }
    }

    // =========================
    // 老师/管理员：Excel批量导入
    // =========================

    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @GetMapping("/import")
    public String importPage() {
        return "attendance-import";
    }

    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @PostMapping("/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "请选择Excel文件");
            return "redirect:/attendance/import";
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            redirectAttributes.addFlashAttribute("error", "文件格式不正确，请上传 .xlsx 或 .xls");
            return "redirect:/attendance/import";
        }

        try {
            File dir = new File(uploadPath);
            if (!dir.exists()) dir.mkdirs();

            String savedName = UUID.randomUUID() + "-" + filename;
            File saved = new File(dir, savedName);
            file.transferTo(saved);

            ImportResult result = attendanceImportService.importFromExcel(saved);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "导入完成：成功 " + result.getSuccessCount() + " 条，失败 " + result.getFailCount() + " 条"
            );
            redirectAttributes.addFlashAttribute("errors", result.getErrors());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "导入失败：" + e.getMessage());
        }

        return "redirect:/attendance/import";
    }

    // =========================
    // 考勤记录列表：
    // 学生只能看自己的；老师/管理员看所有
    // =========================

    @GetMapping("/list")
    public String list(@RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String courseId,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Authentication authentication,
                       Model model) {

        User user = currentUser(authentication);
        String role = roleOf(authentication);
        boolean isTeacherOrAdmin = "TEACHER".equals(role) || "ADMIN".equals(role);

        LocalDate sd = (startDate == null || startDate.isBlank()) ? null : LocalDate.parse(startDate);
        LocalDate ed = (endDate == null || endDate.isBlank()) ? null : LocalDate.parse(endDate);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "checkInTime"));

        // ✅ 老师/管理员：studentId 传 null 表示不过滤；学生：强制传自己的 studentId
        String studentIdFilter = isTeacherOrAdmin ? null : user.getUsername();

        Page<com.example.attendance.entity.Attendance> p =
                attendanceService.list(studentIdFilter, sd, ed, status, courseId, pageable);

        model.addAttribute("records", p.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", p.getTotalPages());
        model.addAttribute("totalElements", p.getTotalElements());

        model.addAttribute("startDate", startDate == null ? "" : startDate);
        model.addAttribute("endDate", endDate == null ? "" : endDate);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("courseId", courseId == null ? "" : courseId);

        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
        model.addAttribute("isTeacherOrAdmin", isTeacherOrAdmin);

        return "attendance-list";
    }

    // =========================
    // 签到会话 — 教师端
    // =========================

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @GetMapping("/session/create")
    public String createSessionPage(Model model, Authentication authentication) {
        User user = currentUser(authentication);
        boolean isAdmin = roleOf(authentication).equals("ADMIN");
        // 管理员看全部课程，教师只看自己的课
        List<Course> courses = isAdmin ?
                courseRepository.findAll(Sort.by("courseId")) :
                courseRepository.findByTeacherId(user.getId());
        model.addAttribute("courses", courses);
        model.addAttribute("isAdmin", isAdmin);
        return "check-in-session-panel";
    }

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @PostMapping("/session/create")
    @ResponseBody
    public String createSession(@RequestParam String courseId, Authentication authentication) {
        try {
            User user = currentUser(authentication);
            CheckInSession session = sessionService.createSession(courseId, user);
            return "/attendance/session/panel/" + session.getId();
        } catch (RuntimeException e) {
            return "ERROR:" + e.getMessage();
        }
    }

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @GetMapping("/session/panel/{id}")
    public String sessionPanel(@PathVariable Long id, Model model) {
        try {
            CheckInSession session = sessionService.findById(id);
            CheckInProgress progress = sessionService.getProgress(id);
            model.addAttribute("s", session);
            model.addAttribute("p", progress);
        } catch (Exception e) {
            model.addAttribute("s", null);
            model.addAttribute("p", null);
            model.addAttribute("errorMsg", e.getMessage());
        }
        return "check-in-session-panel-live";
    }

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @PostMapping("/session/close/{id}")
    public String closeSession(@PathVariable Long id) {
        sessionService.closeSession(id);
        return "redirect:/";
    }

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @PostMapping("/session/extend/{id}")
    public String extendSession(@PathVariable Long id) {
        sessionService.extendSession(id);
        return "redirect:/attendance/session/panel/" + id;
    }

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @PostMapping("/session/manualCheckIn/{id}")
    public String manualCheckIn(@PathVariable Long id,
                                @RequestParam String studentId,
                                RedirectAttributes ra) {
        try {
            sessionService.manualCheckIn(id, studentId.trim());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/attendance/session/panel/" + id;
    }

    // =========================
    // 签到会话 — 学生端
    // =========================

    // 反代签限流
    private final java.util.Map<String, Long> rateLimit = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long RATE_MS = 10 * 60 * 1000;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/session/join")
    @ResponseBody
    public String joinSession(@RequestParam(required = false) Long sessionId,
                              @RequestParam(required = false) String code,
                              Authentication authentication,
                              HttpServletRequest request) {
        try {
            User user = currentUser(authentication);

            CheckInSession session;
            if (sessionId != null) {
                session = sessionService.findById(sessionId);
            } else if (code != null && !code.isBlank()) {
                session = sessionService.findActiveByCode(code.trim());
            } else {
                return "ERROR:请提供口令或选择签到会话";
            }

            // 反代签：同IP+同会话10分钟限签一次
            String rateKey = request.getRemoteAddr() + ":" + session.getId();
            Long last = rateLimit.get(rateKey);
            long now = System.currentTimeMillis();
            if (last != null && (now - last) < RATE_MS) {
                return "ERROR:签到过于频繁，请" + ((RATE_MS - (now - last)) / 1000) + "秒后再试";
            }
            rateLimit.put(rateKey, now);

            boolean ok = studentRepository.findByCourseId(session.getCourseId()).stream()
                    .anyMatch(s -> s.getStudentId().equals(user.getUsername()));
            if (!ok) return "ERROR:你未选此课程";

            attendanceService.checkInViaSession(user.getUsername(), user.getRealName(),
                    session.getCourseId(), request.getRemoteAddr());
            return "签到成功";
        } catch (RuntimeException e) {
            return "ERROR:" + e.getMessage();
        }
    }

    // =========================
    // 教师签到批次历史
    // =========================

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @GetMapping("/session/history")
    public String sessionHistory(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        boolean isAdmin = roleOf(authentication).equals("ADMIN");

        // 查已关闭的会话（最近30条）
        List<CheckInSession> sessions = sessionRepo
                .findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "startTime"))
                .stream()
                .filter(s -> isAdmin || s.getTeacherId().equals(user.getId()))
                .limit(30)
                .collect(java.util.stream.Collectors.toList());

        // 为每个会话计算统计
        List<java.util.Map<String, Object>> summaries = new java.util.ArrayList<>();
        for (CheckInSession s : sessions) {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("courseId", s.getCourseId());
            m.put("courseName", s.getCourseName());
            m.put("startTime", s.getStartTime());
            m.put("endTime", s.getEndTime());
            m.put("code", s.getCode());
            m.put("status", s.getStatus());
            m.put("teacherName", s.getTeacherName());

            try {
                var progress = sessionService.getProgress(s.getId());
                m.put("checkedIn", progress.getCheckedInCount());
                m.put("total", progress.getTotalStudents());
            } catch (Exception e) {
                m.put("checkedIn", 0);
                m.put("total", 0);
            }
            summaries.add(m);
        }

        model.addAttribute("sessions", summaries);
        model.addAttribute("isAdmin", isAdmin);
        return "session-history";
    }

    // =========================
    // API: 签到进度 JSON
    // =========================

    @GetMapping("/api/session/{id}/progress")
    @ResponseBody
    public CheckInProgress progressJson(@PathVariable Long id) {
        return sessionService.getProgress(id);
    }

    // =========================
    // helpers
    // =========================

    private User currentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("当前用户不存在：" + username));
    }

    private String roleOf(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        if (role.startsWith("ROLE_")) role = role.substring("ROLE_".length());
        return role;
    }

    private String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}