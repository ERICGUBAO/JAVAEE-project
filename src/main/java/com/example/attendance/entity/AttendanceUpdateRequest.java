package com.example.attendance.entity;

public class AttendanceUpdateRequest {
    private String studentId;
    private String date;      // 先用字符串，后面可以换 LocalDate
    private String status;    // 例如 "present"/"absent"

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}