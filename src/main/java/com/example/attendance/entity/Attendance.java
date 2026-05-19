package com.example.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Column(name = "course_id", nullable = false, length = 20)
    private String courseId;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    // MySQL: tinyint -> Java: Byte（最匹配）
    @Column(name = "seat_row")
    private Byte seatRow;

    @Column(name = "seat_col")
    private Byte seatCol;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "ip", length = 15)
    private String ip;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // ===== Constructors =====
    public Attendance() {
    }

    // ===== Getters / Setters =====
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Byte getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(Byte seatRow) {
        this.seatRow = seatRow;
    }

    public Byte getSeatCol() {
        return seatCol;
    }

    public void setSeatCol(Byte seatCol) {
        this.seatCol = seatCol;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}