package com.example.attendance.service;

import com.example.attendance.dto.CheckInProgress;
import com.example.attendance.entity.CheckInSession;
import com.example.attendance.entity.User;

import java.util.List;

public interface CheckInSessionService {
    CheckInSession createSession(String courseId, User teacher);
    CheckInSession findById(Long id);
    CheckInSession findActiveByCode(String code);
    void closeSession(Long id);
    void extendSession(Long id);
    CheckInSession findTeacherActiveSession(Long teacherId);
    List<CheckInSession> findActiveForStudent(String studentId);
    CheckInProgress getProgress(Long sessionId);
    void manualCheckIn(Long sessionId, String studentId);
}
