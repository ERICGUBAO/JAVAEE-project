package com.example.attendance.service;

import com.example.attendance.dto.ImportResult;

import java.io.File;

public interface AttendanceImportService {
    ImportResult importFromExcel(File file);
}