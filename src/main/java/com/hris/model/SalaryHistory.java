package com.hris.model;

import com.hris.model.enums.SalaryChangeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Salary History Entity
 * Tracks all salary changes for an employee
 */
@Entity
@Table(name = "salary_history")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"employee", "jobHistory"})
@NoArgsConstructor
@AllArgsConstructor
public class SalaryHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee whose salary history is being tracked
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @ToString.Exclude
    private Employee employee;

    /**
     * Previous salary (NULL for initial salary)
     */
    @Column(name = "old_salary", precision = 15, scale = 2)
    private BigDecimal oldSalary;

    /**
     * New salary after change
     */
    @Column(name = "new_salary", precision = 15, scale = 2, nullable = false)
    private BigDecimal newSalary;

    /**
     * Difference between new and old salary (new - old)
     * Can be positive (increase) or negative (decrease)
     */
    @Column(name = "salary_difference", precision = 15, scale = 2)
    private BigDecimal salaryDifference;

    /**
     * Type of salary change
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 50)
    private SalaryChangeType changeType;

    /**
     * Reason for salary change
     */
    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    /**
     * When this salary change takes effect
     */
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    /**
     * When this salary period ended (NULL if this is the current salary)
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Reference to job history if this salary change is related to a job change
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_history_id")
    @ToString.Exclude
    private EmployeeJobHistory jobHistory;

    /**
     * Who made this change
     */
    @Column(name = "created_by")
    private Long createdBy;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Calculate percentage increase/decrease
     * @return Percentage change (positive for increase, negative for decrease)
     */
    public double getPercentageChange() {
        if (oldSalary == null || oldSalary.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return salaryDifference
                .divide(oldSalary, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Check if this is a salary increase
     * @return true if new salary > old salary
     */
    public boolean isIncrease() {
        return salaryDifference != null && salaryDifference.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if this is a salary decrease
     * @return true if new salary < old salary
     */
    public boolean isDecrease() {
        return salaryDifference != null && salaryDifference.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Check if this is an initial salary (no old salary)
     * @return true if old salary is null
     */
    public boolean isInitialSalary() {
        return oldSalary == null;
    }

    /**
     * Check if this is the current salary
     * @return true if end date is null or end date is after today
     */
    public boolean isCurrentSalary() {
        return endDate == null || endDate.isAfter(LocalDate.now());
    }
}
