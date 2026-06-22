package com.example.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class CheckInProgress {
    private String courseName;
    private String courseId;
    private String code;
    private int checkedInCount;
    private int totalStudents;
    private String status;
    private List<String> checkedNames = new ArrayList<>();
    private List<String> uncheckedNames = new ArrayList<>();

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getCheckedInCount() { return checkedInCount; }
    public void setCheckedInCount(int checkedInCount) { this.checkedInCount = checkedInCount; }
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getCheckedNames() { return checkedNames; }
    public void setCheckedNames(List<String> checkedNames) { this.checkedNames = checkedNames; }
    public List<String> getUncheckedNames() { return uncheckedNames; }
    public void setUncheckedNames(List<String> uncheckedNames) { this.uncheckedNames = uncheckedNames; }
}
