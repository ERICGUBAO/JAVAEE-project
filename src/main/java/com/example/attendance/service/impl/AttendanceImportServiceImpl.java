package com.example.attendance.service.impl;

import com.example.attendance.dto.ImportResult;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.User;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.AttendanceImportService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceImportServiceImpl implements AttendanceImportService {

    private static final Set<String> ALLOWED_STATUS = Set.of("NORMAL", "LATE", "EARLY");
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    );

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public AttendanceImportServiceImpl(AttendanceRepository attendanceRepository,
                                       UserRepository userRepository,
                                       CourseRepository courseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public ImportResult importFromExcel(File file) {
        ImportResult result = new ImportResult();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() < 1) {
                result.incFail(1, "Excel没有数据行");
                return result;
            }

            // 从第2行开始（i=1）跳过表头
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                int rowNumForUser = i + 1; // 用户看到的行号

                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String studentId = cellString(row.getCell(0));
                    String courseId = cellString(row.getCell(1));
                    String checkInStr = cellString(row.getCell(2));
                    String checkOutStr = cellString(row.getCell(3));
                    String status = cellString(row.getCell(4));

                    if (studentId.isBlank() || courseId.isBlank() || checkInStr.isBlank()) {
                        result.incFail(rowNumForUser, "student_id/course_id/check_in_time 不能为空");
                        continue;
                    }

                    Optional<User> userOpt = userRepository.findByUsername(studentId);
                    if (userOpt.isEmpty()) {
                        result.incFail(rowNumForUser, "学生不存在：" + studentId);
                        continue;
                    }

                    Optional<Course> courseOpt = courseRepository.findById(courseId);
                    if (courseOpt.isEmpty()) {
                        result.incFail(rowNumForUser, "课程不存在：" + courseId);
                        continue;
                    }

                    LocalDateTime checkInTime = parseDateTime(checkInStr);
                    if (checkInTime == null) {
                        result.incFail(rowNumForUser, "check_in_time 格式错误，支持格式：yyyy/MM/dd HH:mm:ss 或 yyyy-MM-dd HH:mm:ss");
                        continue;
                    }

                    LocalDateTime checkOutTime = null;
                    if (!checkOutStr.isBlank()) {
                        checkOutTime = parseDateTime(checkOutStr);
                        if (checkOutTime == null) {
                            result.incFail(rowNumForUser, "check_out_time 格式错误，支持格式：yyyy/MM/dd HH:mm:ss 或 yyyy-MM-dd HH:mm:ss 或留空");
                            continue;
                        }
                        if (checkOutTime.isBefore(checkInTime)) {
                            result.incFail(rowNumForUser, "check_out_time 不能早于 check_in_time");
                            continue;
                        }
                    }

                    // status 可空：空则自动计算
                    if (!status.isBlank()) {
                        status = status.trim().toUpperCase();
                        if (!ALLOWED_STATUS.contains(status)) {
                            result.incFail(rowNumForUser, "status 只能是 NORMAL/LATE/EARLY 或留空");
                            continue;
                        }
                    } else {
                        status = calcStatusFromCourse(courseOpt.get(), checkInTime, checkOutTime);
                    }

                    User user = userOpt.get();

                    Attendance attendance = new Attendance();
                    attendance.setStudentId(user.getUsername());
                    attendance.setStudentName(user.getRealName());
                    attendance.setCourseId(courseId);

                    attendance.setCheckInTime(checkInTime);
                    attendance.setCheckOutTime(checkOutTime);

                    attendance.setAttendDate(checkInTime.toLocalDate());
                    attendance.setStatus(status);

                    attendance.setIp("IMPORT");
                    attendance.setCreateTime(LocalDateTime.now());

                    // 这里依赖你唯一索引：student_id + course_id + attend_date
                    // 发生重复 => 记失败（不覆盖）
                    try {
                        attendanceRepository.save(attendance);
                        result.incSuccess();
                    } catch (DataIntegrityViolationException dup) {
                        result.incFail(rowNumForUser, "重复记录（同学生同课程同日期已存在）");
                    }

                } catch (Exception ex) {
                    result.incFail(rowNumForUser, "解析失败：" + ex.getMessage());
                }
            }

        } catch (Exception ex) {
            result.incFail(1, "读取Excel失败：" + ex.getMessage());
        }

        return result;
    }

    /**
     * 从单元格获取字符串值，自动处理日期类型单元格
     */
    private String cellString(Cell cell) {
        if (cell == null) return "";

        // 处理日期类型的单元格
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            LocalDateTime dateTime = cell.getLocalDateTimeCellValue();
            return dateTime.format(DATE_TIME_FORMATTERS.get(0)); // 使用 yyyy/MM/dd HH:mm:ss 格式
        }

        // 处理字符串类型
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue();
            return value == null ? "" : value.trim();
        }

        // 处理数值类型（可能是纯数字）
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }

        // 其他类型统一转字符串
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return value == null ? "" : value.trim();
    }

    /**
     * 解析日期时间字符串，支持多种格式
     */
    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;

        String trimmed = s.trim();

        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (Exception ignored) {
                // 继续尝试下一个格式
            }
        }

        return null;
    }

    
    /**
     * status留空时按你当前口径计算：
     * - 迟到：checkIn > classStart + 5min => LATE 否则 NORMAL
     * - 早退：如果有checkOut 且 checkOut < classEnd - 5min => EARLY（覆盖前面）
     *
     * 注意：这要求课程 start_time/end_time 有值，否则退化为 NORMAL。
     */
    private String calcStatusFromCourse(Course course, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (course.getStartTime() == null || course.getEndTime() == null) {
            return "NORMAL";
        }

        LocalDate d = checkIn.toLocalDate();
        LocalDateTime classStart = LocalDateTime.of(d, course.getStartTime());
        LocalDateTime classEnd = LocalDateTime.of(d, course.getEndTime());

        String status = checkIn.isAfter(classStart.plusMinutes(5)) ? "LATE" : "NORMAL";

        if (checkOut != null && checkOut.isBefore(classEnd.minusMinutes(5))) {
            status = "EARLY";
        }
        return status;
    }
}