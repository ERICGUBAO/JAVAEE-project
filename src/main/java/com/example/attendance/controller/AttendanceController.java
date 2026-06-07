package com.example.attendance.controller;

import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.User;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.AttendanceImportService;
import com.example.attendance.service.AttendanceService;
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
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    public AttendanceController(AttendanceService attendanceService,
                                AttendanceImportService attendanceImportService,
                                CourseRepository courseRepository,
                                UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.attendanceImportService = attendanceImportService;
        this.courseRepository = courseRepository;
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

        model.addAttribute("startDate", startDate == null ? "" : startDate);
        model.addAttribute("endDate", endDate == null ? "" : endDate);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("courseId", courseId == null ? "" : courseId);

        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseId")));
        model.addAttribute("isTeacherOrAdmin", isTeacherOrAdmin);

        return "attendance-list";
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