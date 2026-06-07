package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.service.AttendanceService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final int LATE_GRACE_MINUTES = 5;   // 迟到容错
    private static final int EARLY_GRACE_MINUTES = 5;  // 早退容错

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 CourseRepository courseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void checkIn(String studentId, String studentName, String courseId, String ip) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在：" + courseId));

        if (course.getStartTime() == null || course.getEndTime() == null) {
            throw new RuntimeException("课程未配置 start_time/end_time，无法打卡");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        LocalDateTime classStart = LocalDateTime.of(today, course.getStartTime());
        LocalDateTime classEnd = LocalDateTime.of(today, course.getEndTime());

        // ✅ 打卡允许窗口：开始前15分钟 ~ 开始后30分钟
        LocalDateTime earliest = classStart.minusMinutes(15);
        LocalDateTime latest = classStart.plusMinutes(30);
        if (now.isBefore(earliest) || now.isAfter(latest)) {
            throw new RuntimeException("不在打卡时间范围内（允许：" +
                    earliest.toLocalTime() + " ~ " + latest.toLocalTime() + "）");
        }

        // ✅ 查“今天这门课”的记录（唯一约束保证至多一条）
        Attendance attendance = attendanceRepository
                .findByStudentIdAndCourseIdAndAttendDate(studentId, courseId, today)
                .orElseGet(Attendance::new);

        // 如果已签到，阻止重复签到（也可以改成直接提示）
        if (attendance.getId() != null && attendance.getCheckInTime() != null) {
            throw new RuntimeException("你今天已签到，无需重复打卡");
        }

        // ✅ 迟到判定：晚于 classStart + 5分钟 => LATE
        boolean isLate = now.isAfter(classStart.plusMinutes(LATE_GRACE_MINUTES));
        String status = isLate ? "LATE" : "NORMAL";

        attendance.setStudentId(studentId);
        attendance.setStudentName(studentName);
        attendance.setCourseId(courseId);
        attendance.setAttendDate(today);

        attendance.setCheckInTime(now);
        attendance.setStatus(status);
        attendance.setIp(ip);
        attendance.setCreateTime(now);

        attendanceRepository.save(attendance);
    }

    @Override
    public void checkOut(String studentId, String courseId, String ip) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在：" + courseId));

        if (course.getStartTime() == null || course.getEndTime() == null) {
            throw new RuntimeException("课程未配置 start_time/end_time，无法签退");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Attendance attendance = attendanceRepository
                .findByStudentIdAndCourseIdAndAttendDate(studentId, courseId, today)
                .orElseThrow(() -> new RuntimeException("你今天还没有签到，无法签退"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("你今天已签退，无需重复签退");
        }

        LocalDateTime classEnd = LocalDateTime.of(today, course.getEndTime());

        // ✅ 早退判定：签退时间 < 下课时间 - 5分钟 => EARLY
        boolean isEarly = now.isBefore(classEnd.minusMinutes(EARLY_GRACE_MINUTES));
        if (isEarly) {
            attendance.setStatus("EARLY");
        }
        attendance.setCheckOutTime(now);
        attendance.setIp(ip);

        attendanceRepository.save(attendance);
    }

    @Override
    public Page<Attendance> list(String studentId,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 String status,
                                 String courseId,
                                 Pageable pageable) {

        Specification<Attendance> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (studentId != null && !studentId.isBlank()) {
                p = cb.and(p, cb.equal(root.get("studentId"), studentId));
            }

            if (startDate != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("checkInTime"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                p = cb.and(p, cb.lessThan(root.get("checkInTime"), endDate.plusDays(1).atStartOfDay()));
            }

            if (status != null && !status.isBlank()) {
                p = cb.and(p, cb.equal(root.get("status"), status));
            }

            if (courseId != null && !courseId.isBlank()) {
                p = cb.and(p, cb.equal(root.get("courseId"), courseId));
            }

            return p;
        };

        return attendanceRepository.findAll(spec, pageable);
    }
}