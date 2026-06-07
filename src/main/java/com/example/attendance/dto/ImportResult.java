package com.example.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int successCount;
    private int failCount;
    private List<ImportErrorRow> errors = new ArrayList<>();

    public int getSuccessCount() { return successCount; }
    public int getFailCount() { return failCount; }
    public List<ImportErrorRow> getErrors() { return errors; }

    public void incSuccess() { successCount++; }
    public void incFail(int rowNum, String reason) {
        failCount++;
        errors.add(new ImportErrorRow(rowNum, reason));
    }
}