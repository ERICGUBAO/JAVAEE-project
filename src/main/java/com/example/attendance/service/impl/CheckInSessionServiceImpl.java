package com.example.attendance.service.impl;

import com.example.attendance.dto.CheckInProgress;
import com.example.attendance.entity.*;
import com.example.attendance.repository.*;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CheckInSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckInSessionServiceImpl implements CheckInSessionService {

    private final CheckInSessionRepository sessionRepo;
    private final CourseRepository courseRepo;
    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final AttendanceRepository attendanceRepo;
    private final AttendanceService attendanceService;

    public CheckInSessionServiceImpl(CheckInSessionRepository sessionRepo,
                                     CourseRepository courseRepo,
                                     StudentRepository studentRepo,
                                     UserRepository userRepo,
                                     AttendanceRepository attendanceRepo,
                                     AttendanceService attendanceService) {
        this.sessionRepo = sessionRepo;
        this.courseRepo = courseRepo;
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
        this.attendanceRepo = attendanceRepo;
        this.attendanceService = attendanceService;
    }

    @Override
    @Transactional
    public CheckInSession createSession(String courseId, User teacher) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 关闭同教师同课程之前的活跃会话
        List<CheckInSession> oldSessions = sessionRepo.findByTeacherIdAndStatus(teacher.getId(), "ACTIVE");
        for (CheckInSession old : oldSessions) {
            if (old.getCourseId().equals(courseId)) {
                old.setStatus("CLOSED");
                sessionRepo.save(old);
            }
        }

        String code = String.format("%04d", new Random().nextInt(10000));
        LocalDateTime now = LocalDateTime.now();

        CheckInSession s = new CheckInSession();
        s.setCourseId(courseId);
        s.setCourseName(course.getCourseName());
        s.setTeacherId(teacher.getId());
        s.setTeacherName(teacher.getRealName());
        s.setCode(code);
        s.setStartTime(now);
        s.setEndTime(now.plusMinutes(2));
        s.setStatus("ACTIVE");
        return sessionRepo.save(s);
    }

    @Override
    public CheckInSession findById(Long id) {
        return sessionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
    }

    @Override
    public CheckInSession findActiveByCode(String code) {
        // 查找所有ACTIVE会话，然后内存过滤code
        return sessionRepo.findAll().stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .filter(s -> s.getCode().equals(code))
                .filter(s -> s.getEndTime().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("口令无效或签到已过期"));
    }

    @Override
    public void closeSession(Long id) {
        CheckInSession s = findById(id);
        s.setStatus("CLOSED");
        sessionRepo.save(s);
    }

    @Override
    public void extendSession(Long id) {
        CheckInSession s = findById(id);
        s.setEndTime(s.getEndTime().plusMinutes(1));
        sessionRepo.save(s);
    }

    @Override
    public CheckInSession findTeacherActiveSession(Long teacherId) {
        List<CheckInSession> list = sessionRepo.findByTeacherIdAndStatus(teacherId, "ACTIVE");
        // 过滤已过期的
        LocalDateTime now = LocalDateTime.now();
        return list.stream()
                .filter(s -> s.getEndTime().isAfter(now))
                .findFirst().orElse(null);
    }

    @Override
    public List<CheckInSession> findActiveForStudent(String studentId) {
        // 查学生选的课
        List<String> courseIds = studentRepo.findByStudentId(studentId).stream()
                .map(Student::getCourseId).distinct().collect(Collectors.toList());
        if (courseIds.isEmpty()) return Collections.emptyList();

        // 查这些课的活跃会话
        LocalDateTime now = LocalDateTime.now();
        return sessionRepo.findByCourseIdInAndStatus(courseIds, "ACTIVE").stream()
                .filter(s -> s.getEndTime().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    public CheckInProgress getProgress(Long sessionId) {
        CheckInSession session = findById(sessionId);
        Course course = courseRepo.findById(session.getCourseId()).orElse(null);

        CheckInProgress p = new CheckInProgress();
        p.setCode(session.getCode());
        p.setCourseId(session.getCourseId());
        p.setCourseName(course != null ? course.getCourseName() : session.getCourseId());
        p.setStatus(session.getStatus());
        p.setTotalStudents(0);
        p.setCheckedInCount(0);
        p.setCheckedNames(Collections.emptyList());
        p.setUncheckedNames(Collections.emptyList());

        try {
            // 选课学生
            List<Student> enrolled = studentRepo.findByCourseId(session.getCourseId());
            Set<String> enrolledNames = enrolled.stream()
                    .map(Student::getStudentName).collect(Collectors.toSet());
            p.setTotalStudents(enrolledNames.size());

            // 限定会话时间窗口内的考勤记录（防止跨会话混数据）
            Set<String> checkedNames = new java.util.HashSet<>();
            List<Attendance> records = attendanceRepo.findAll();
            for (Attendance a : records) {
                if (session.getCourseId().equals(a.getCourseId())
                        && a.getCheckInTime() != null
                        && !a.getCheckInTime().isBefore(session.getStartTime())
                        && !a.getCheckInTime().isAfter(session.getEndTime())) {
                    checkedNames.add(a.getStudentName());
                }
            }

            p.setCheckedInCount(checkedNames.size());
            p.setCheckedNames(new ArrayList<>(checkedNames));

            List<String> unchecked = new ArrayList<>();
            for (String name : enrolledNames) {
                if (!checkedNames.contains(name)) unchecked.add(name);
            }
            p.setUncheckedNames(unchecked);

        } catch (Exception e) {
            // 即使查考勤失败也返回基本信息
        }

        return p;
    }

    @Override
    @Transactional
    public void manualCheckIn(Long sessionId, String studentId) {
        CheckInSession session = findById(sessionId);
        if (!"ACTIVE".equals(session.getStatus()))
            throw new RuntimeException("会话已关闭");

        User user = userRepo.findByUsername(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在：" + studentId));

        // 自动选课（如果未选）
        boolean enrolled = studentRepo.findByCourseId(session.getCourseId()).stream()
                .anyMatch(s -> s.getStudentId().equals(studentId));
        if (!enrolled) {
            Student st = new Student();
            st.setStudentId(studentId);
            st.setStudentName(user.getRealName());
            st.setCourseId(session.getCourseId());
            studentRepo.save(st);
        }

        // 调用统一的签到逻辑（含去重检查、迟到判定）
        attendanceService.checkInViaSession(
                studentId, user.getRealName(), session.getCourseId(), "MANUAL");
    }
}
