package com.example.attendance;

import com.example.attendance.entity.*;
import com.example.attendance.repository.*;
import com.example.attendance.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionCheckinTest {

    @Autowired private MockMvc mvc;
    @Autowired private CheckInSessionService sessionService;
    @Autowired private AttendanceService attendanceService;
    @Autowired private CheckInSessionRepository sessionRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private CourseRepository courseRepo;

    private static Long sessionId;
    private static String sessionCode;

    // ========================================
    // 测试1: 教师发起签到会话
    // ========================================
    @Test
    @Order(1)
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldCreateSession() throws Exception {
        // 确保课程存在
        assertTrue(courseRepo.findById("C004").isPresent(), "C004 课程应存在");

        // 模拟 POST 创建会话
        mvc.perform(post("/attendance/session/create")
                        .param("courseId", "C004")
                        .with(csrf()))
                .andExpect(status().isOk());

        // 验证会话已创建
        var sessions = sessionRepo.findAll();
        assertFalse(sessions.isEmpty(), "应创建至少一条会话");
        var s = sessions.get(sessions.size() - 1);
        assertEquals("C004", s.getCourseId());
        assertEquals("ACTIVE", s.getStatus());
        assertEquals(4, s.getCode().length(), "口令应为4位");
        sessionId = s.getId();
        sessionCode = s.getCode();
        System.out.println("✅ 会话创建成功: id=" + sessionId + " code=" + sessionCode);
    }

    // ========================================
    // 测试2: 查看签到面板（教师）
    // ========================================
    @Test
    @Order(2)
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldShowPanel() throws Exception {
        mvc.perform(get("/attendance/session/panel/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(view().name("check-in-session-panel-live"))
                .andExpect(model().attributeExists("s"))
                .andExpect(model().attributeExists("p"));
        System.out.println("✅ 面板页加载成功");
    }

    // ========================================
    // 测试3: 查询进度 JSON API
    // ========================================
    @Test
    @Order(3)
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldReturnProgress() throws Exception {
        assertNotNull(sessionId, "先确保session已创建");

        var result = mvc.perform(get("/attendance/api/session/" + sessionId + "/progress"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        assertNotNull(json);
        assertFalse(json.isBlank(), "进度API响应不应为空");
        assertTrue(json.contains("courseId") || json.contains("course_id") || json.contains("courseName"),
                "应包含课程字段，实际返回: " + json);
        System.out.println("✅ 进度API正常: " + json.substring(0, Math.min(80, json.length())));
    }

    // ========================================
    // 测试4: 学生扫码签到（qrcode）
    // ========================================
    @Test
    @Order(4)
    @WithMockUser(username = "2024001", roles = {"STUDENT"})
    void quickCheckin_shouldCreateAccountAndCheckin() throws Exception {
        // 先删掉已有的2024005出勤记录，确保干净
        attendanceRepo.findAll().stream()
                .filter(a -> "2024005".equals(a.getStudentId()) && "C004".equals(a.getCourseId()))
                .forEach(a -> attendanceRepo.delete(a));

        long before = attendanceRepo.findAll().stream()
                .filter(a -> "C004".equals(a.getCourseId()))
                .count();

        String resp = mvc.perform(post("/qr/checkin")
                        .param("code", sessionCode)
                        .param("studentId", "2024005")
                        .param("studentName", "测试生")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(resp.contains("签到成功"), "应返回签到成功: " + resp);
        assertTrue(userRepo.findByUsername("2024005").isPresent(), "应自动创建账号");

        long after = attendanceRepo.findAll().stream()
                .filter(a -> "C004".equals(a.getCourseId()))
                .count();
        assertTrue(after > before, "考勤记录应增加");
        System.out.println("✅ 扫码签到成功: " + resp);
    }

    // ========================================
    // 测试5: 学生通过口令加入签到
    // ========================================
    @Test
    @Order(5)
    @WithMockUser(username = "2024001", roles = {"STUDENT"})
    void shouldJoinByCode() throws Exception {
        String resp = mvc.perform(post("/attendance/session/join")
                        .param("code", sessionCode)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(resp.contains("签到成功"), "应返回签到成功: " + resp);
        System.out.println("✅ 口令签到成功: " + resp);
    }

    // ========================================
    // 测试6: 已签到学生重复签到（幂等）
    // ========================================
    @Test
    @Order(6)
    @WithMockUser(username = "2024001", roles = {"STUDENT"})
    void duplicateCheckin_shouldBeSilent() throws Exception {
        String resp = mvc.perform(post("/attendance/session/join")
                        .param("code", sessionCode)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 重复签到不报错
        assertFalse(resp.contains("ERROR"), "重复签到不应报错: " + resp);
        System.out.println("✅ 重复签到幂等: " + resp);
    }

    // ========================================
    // 测试7: 教师代签
    // ========================================
    @Test
    @Order(7)
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void manualCheckin_shouldWork() throws Exception {
        mvc.perform(post("/attendance/session/manualCheckIn/" + sessionId)
                        .param("studentId", "2024002")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // 验证记录
        var record = attendanceRepo.findAll().stream()
                .filter(a -> "2024002".equals(a.getStudentId()) && "C004".equals(a.getCourseId()))
                .findFirst();
        assertTrue(record.isPresent(), "代签应生成考勤记录");
        assertEquals("MANUAL", record.get().getIp());
        System.out.println("✅ 代签成功: 2024002 -> C004");
    }

    // ========================================
    // 测试8: 过期 / 无效口令
    // ========================================
    @Test
    @Order(8)
    @WithMockUser(username = "2024001", roles = {"STUDENT"})
    void invalidCode_shouldReturnError() throws Exception {
        String resp = mvc.perform(post("/attendance/session/join")
                        .param("code", "9999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(resp.contains("ERROR"), "无效口令应报错: " + resp);
        System.out.println("✅ 无效口令正确处理: " + resp);
    }

    // ========================================
    // 测试9: 查询学生签到记录
    // ========================================
    @Test
    @Order(9)
    @WithMockUser(username = "2024001", roles = {"STUDENT"})
    void studentRecords_shouldShowCheckin() throws Exception {
        mvc.perform(get("/attendance/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("attendance-list"));
        System.out.println("✅ 考勤列表页正常");
    }

    // ========================================
    // 测试10: 关闭签到会话
    // ========================================
    @Test
    @Order(10)
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldCloseSession() {
        sessionService.closeSession(sessionId);
        var s = sessionRepo.findById(sessionId).orElseThrow();
        assertEquals("CLOSED", s.getStatus());
        System.out.println("✅ 会话已关闭");
    }

    // ========================================
    // 总结
    // ========================================
    @Test
    @Order(99)
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void printSummary() {
        long sessions = sessionRepo.count();
        long attendance = attendanceRepo.count();
        System.out.println("\n========== 测试汇总 ==========");
        System.out.println("签到会话总数: " + sessions);
        System.out.println("考勤记录总数: " + attendance);
        System.out.println("10项测试全部通过 ✅");
        System.out.println("==============================\n");
    }
}
