package com.hris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Employee Shift Setting Entity
 * Assignment Shift Pattern ke Employee dengan Date Range
 */
@Entity
@Table(name = "employee_shift_settings", indexes = {
    @Index(name = "idx_employee_date", columnList = "employee_id, effective_from, effective_to")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShiftSetting extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "shift_pattern_id", nullable = false)
    private Long shiftPatternId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_pattern_id", insertable = false, updatable = false)
    private ShiftPattern shiftPattern;

    /**
     * Check if this setting is currently active
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !effectiveFrom.isAfter(today) &&
               (effectiveTo == null || !effectiveTo.isBefore(today));
    }

    /**
     * Check if this setting is active on a specific date
     */
    public boolean isActiveOn(LocalDate date) {
        return !effectiveFrom.isAfter(date) &&
               (effectiveTo == null || !effectiveTo.isBefore(date));
    }

    /**
     * Check if this setting has ended
     */
    public boolean isEnded() {
        return effectiveTo != null && effectiveTo.isBefore(LocalDate.now());
    }

    /**
     * Get status for display
     */
    public String getStatus() {
        if (isEnded()) {
            return "Ended";
        }
        if (isActive()) {
            return "Active";
        }
        return "Future";
    }
}
