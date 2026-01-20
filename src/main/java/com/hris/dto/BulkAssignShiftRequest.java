package com.hris.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for Bulk Shift Assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAssignShiftRequest {

    private List<Long> employeeIds;
    private Long shiftPatternId;
    private LocalDate effectiveFrom;
    private String reason;
    private String notes;

    /**
     * Check if this is a retroactive assignment (past date)
     */
    public boolean isRetroactive() {
        return effectiveFrom != null && effectiveFrom.isBefore(LocalDate.now());
    }

    /**
     * Get days ago for retroactive assignment
     */
    public long getRetroactiveDays() {
        if (!isRetroactive()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(effectiveFrom, LocalDate.now());
    }
}
