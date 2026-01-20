package com.hris.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO for Bulk Shift Assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAssignShiftResult {

    @Builder.Default
    private List<SuccessItem> successList = new ArrayList<>();

    @Builder.Default
    private List<FailureItem> failureList = new ArrayList<>();

    @Builder.Default
    private List<SkippedItem> skippedList = new ArrayList<>();

    private boolean retroactive;
    private long retroactiveDays;

    public int getTotalProcessed() {
        return successList.size() + failureList.size() + skippedList.size();
    }

    public int getSuccessCount() {
        return successList.size();
    }

    public int getFailureCount() {
        return failureList.size();
    }

    public int getSkippedCount() {
        return skippedList.size();
    }

    public double getSuccessPercentage() {
        int total = getTotalProcessed();
        if (total == 0) return 0;
        return (getSuccessCount() * 100.0) / total;
    }

    /**
     * Success item - assignment successful
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuccessItem {
        private Long employeeId;
        private String employeeName;
        private String departmentName;
        private String positionName;
        private Long assignmentId;
        private String previousShiftName;
        private String newShiftName;
        private LocalDate effectiveFrom;
        private LocalDate previousClosedOn;
        private boolean wasOverride;
    }

    /**
     * Failure item - assignment failed
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailureItem {
        private Long employeeId;
        private String employeeName;
        private String departmentName;
        private String positionName;
        private String errorMessage;
        private String errorType; // EMPLOYEE_NOT_FOUND, INACTIVE, OVERLAP, etc
    }

    /**
     * Skipped item - assignment skipped (same pattern, etc)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkippedItem {
        private Long employeeId;
        private String employeeName;
        private String departmentName;
        private String positionName;
        private String skipReason; // SAME_PATTERN, ALREADY_ASSIGNED, etc
        private String currentShiftName;
    }
}
