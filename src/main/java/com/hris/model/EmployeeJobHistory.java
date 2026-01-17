package com.hris.model;

import com.hris.model.enums.ChangeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employee Job History Entity
 * Tracks department/position changes, promotions, transfers, etc.
 */
@Entity
@Table(name = "employee_job_history")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"employee", "department", "position"})
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJobHistory extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee whose job history is being tracked
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @ToString.Exclude
    private Employee employee;

    /**
     * Department during this job period
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @ToString.Exclude
    private Department department;

    /**
     * Position during this job period
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    @ToString.Exclude
    private Position position;

    /**
     * Type of job change
     * HIRE, PROMOTION, TRANSFER, DEMOTION, RESIGNATION
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 50)
    private ChangeType changeType;

    /**
     * Reason for the job change
     */
    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    /**
     * When this job started/changed
     */
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    /**
     * When this job ended (NULL if this is the current job)
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * TRUE if this is the employee's current job
     */
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    /**
     * Salary at the time of this job (for historical reference)
     */
    @Column(name = "salary_at_time", precision = 15, scale = 2)
    private BigDecimal salaryAtTime;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Calculate duration of this job in days
     * @return Duration in days (0 if not ended yet)
     */
    public long getDurationDays() {
        if (endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(effectiveDate, endDate);
    }

    /**
     * Calculate duration of this job in months (approximate)
     * @return Duration in months (0 if not ended yet)
     */
    public long getDurationMonths() {
        if (endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MONTHS.between(effectiveDate, endDate);
    }

    /**
     * Calculate duration of this job in years (approximate)
     * @return Duration in years (0 if not ended yet)
     */
    public double getDurationYears() {
        if (endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.YEARS.between(effectiveDate, endDate);
    }

    /**
     * Check if this job is currently active
     * @return true if end date is null or end date is after today
     */
    public boolean isActive() {
        return endDate == null || endDate.isAfter(LocalDate.now());
    }
}
