package com.example.attendance.dto;

public class ImportErrorRow {
    private int rowNum;      // Excel 行号（从1开始更符合用户直觉）
    private String reason;   // 失败原因

    public ImportErrorRow() {}

    public ImportErrorRow(int rowNum, String reason) {
        this.rowNum = rowNum;
        this.reason = reason;
    }

    public int getRowNum() { return rowNum; }
    public void setRowNum(int rowNum) { this.rowNum = rowNum; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}