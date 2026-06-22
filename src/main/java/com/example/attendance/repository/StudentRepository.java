package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    // 搜索：按学号/姓名模糊匹配（分页 + 排序由 Pageable 决定）
    Page<Student> findByStudentIdContainingAndStudentNameContaining(
            String studentId,
            String studentName,
            Pageable pageable
    );

    /** 查询某课程的所有选课学生 */
    List<Student> findByCourseId(String courseId);

    /** 某课程的选课人数 */
    long countByCourseId(String courseId);

    /** 根据学号精确查询选课记录 */
    List<Student> findByStudentId(String studentId);

    /** 多条件分页搜索（AND 逻辑） */
    Page<Student> findByStudentIdContainingAndStudentNameContainingAndCourseIdContainingAndDepartmentContaining(
            String studentId, String studentName, String courseId, String department, Pageable pageable);

    /** 查所有不重复的学院名 */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s.department FROM Student s WHERE s.department IS NOT NULL AND s.department != ''")
    List<String> findAllDepartments();
}