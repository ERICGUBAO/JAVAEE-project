package com.example.attendance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_selection")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号长度不能超过20")
    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Size(max = 30, message = "学院名长度不能超过30")
    @Column(name = "department", length = 30)
    private String department;

    // 允许空；如果填，只能 M/F
    @Pattern(regexp = "^(|M|F)$", message = "性别只能是 M 或 F")
    @Column(name = "gender", columnDefinition = "char(1)")
    private String gender;

    @NotBlank(message = "课程号不能为空")
    @Size(max = 20, message = "课程号长度不能超过20")
    @Column(name = "course_id", nullable = false, length = 20)
    private String courseId;

    // ✅ datetime-local 默认格式：yyyy-MM-dd'T'HH:mm
    // （即使你最终改成只读显示，这个注解也不会有坏处）
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "select_time")
    private LocalDateTime selectTime;

    // ✅ date 默认格式：yyyy-MM-dd
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 20, message = "联系方式长度不能超过20")
    @Pattern(regexp = "^(|[0-9+\\- ]{6,20})$", message = "联系方式格式不正确")
    @Column(name = "phone", length = 20)
    private String phone;

    public Student() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public LocalDateTime getSelectTime() { return selectTime; }
    public void setSelectTime(LocalDateTime selectTime) { this.selectTime = selectTime; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}