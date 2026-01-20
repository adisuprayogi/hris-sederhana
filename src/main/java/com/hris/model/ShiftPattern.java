package com.hris.model;

import com.hris.model.enums.ShiftType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Shift Pattern Entity
 * Pattern (Shift Package + Permissions) - Layer 3 dari Shift System
 */
@Entity
@Table(name = "shift_patterns")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShiftPattern extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "shift_package_id", nullable = false)
    private Long shiftPackageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", length = 20)
    private ShiftType shiftType = ShiftType.FIXED;

    @Column(name = "flexible_start_window_start")
    private LocalTime flexibleStartWindowStart;

    @Column(name = "flexible_start_window_end")
    private LocalTime flexibleStartWindowEnd;

    @Column(name = "flexible_required_hours", precision = 4, scale = 2)
    private BigDecimal flexibleRequiredHours;

    @Column(name = "is_overtime_allowed")
    private Boolean isOvertimeAllowed = false;

    @Column(name = "is_wfh_allowed")
    private Boolean isWfhAllowed = false;

    @Column(name = "is_attendance_mandatory")
    private Boolean isAttendanceMandatory = true;

    @Column(name = "late_tolerance_minutes")
    private Integer lateToleranceMinutes = 0;

    @Column(name = "early_leave_tolerance_minutes")
    private Integer earlyLeaveToleranceMinutes = 0;

    @Column(name = "late_deduction_per_minute", precision = 15, scale = 2)
    private BigDecimal lateDeductionPerMinute = BigDecimal.ZERO;

    @Column(name = "late_deduction_max_amount", precision = 15, scale = 2)
    private BigDecimal lateDeductionMaxAmount = BigDecimal.ZERO;

    @Column(name = "underwork_deduction_per_minute", precision = 15, scale = 2)
    private BigDecimal underworkDeductionPerMinute = BigDecimal.ZERO;

    @Column(name = "underwork_deduction_max_amount", precision = 15, scale = 2)
    private BigDecimal underworkDeductionMaxAmount = BigDecimal.ZERO;

    @Column(name = "color", length = 20)
    private String color = "#3B82F6";

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // Holiday Override Settings
    @Column(name = "override_national_holiday")
    private Boolean overrideNationalHoliday = false;

    @Column(name = "override_company_holiday")
    private Boolean overrideCompanyHoliday = false;

    @Column(name = "override_joint_leave")
    private Boolean overrideJointLeave = false;

    @Column(name = "override_weekly_leave")
    private Boolean overrideWeeklyLeave = false;

    // Relations
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_package_id", insertable = false, updatable = false)
    private ShiftPackage shiftPackage;

    /**
     * Check if this is a flexible shift pattern
     */
    public boolean isFlexible() {
        return shiftType == ShiftType.FLEXIBLE;
    }

    /**
     * Check if this is a rotating shift pattern
     */
    public boolean isRotating() {
        return shiftType == ShiftType.ROTATING;
    }

    /**
     * Get total tolerance minutes for calculation
     */
    public int getTotalToleranceMinutes() {
        int total = 0;
        if (lateToleranceMinutes != null) {
            total += lateToleranceMinutes;
        }
        if (earlyLeaveToleranceMinutes != null) {
            total += earlyLeaveToleranceMinutes;
        }
        return total;
    }

    /**
     * Calculate late deduction
     */
    public BigDecimal calculateLateDeduction(int lateMinutes) {
        if (lateDeductionPerMinute == null || lateDeductionPerMinute.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Subtract tolerance
        int billableMinutes = Math.max(0, lateMinutes - (lateToleranceMinutes != null ? lateToleranceMinutes : 0));

        BigDecimal deduction = lateDeductionPerMinute.multiply(BigDecimal.valueOf(billableMinutes));

        // Cap at max amount
        if (lateDeductionMaxAmount != null && lateDeductionMaxAmount.compareTo(BigDecimal.ZERO) > 0) {
            deduction = deduction.min(lateDeductionMaxAmount);
        }

        return deduction;
    }

    /**
     * Calculate underwork deduction
     */
    public BigDecimal calculateUnderworkDeduction(int underworkMinutes) {
        if (underworkDeductionPerMinute == null || underworkDeductionPerMinute.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal deduction = underworkDeductionPerMinute.multiply(BigDecimal.valueOf(underworkMinutes));

        // Cap at max amount
        if (underworkDeductionMaxAmount != null && underworkDeductionMaxAmount.compareTo(BigDecimal.ZERO) > 0) {
            deduction = deduction.min(underworkDeductionMaxAmount);
        }

        return deduction;
    }

    /**
     * Get display summary
     */
    public String getDisplaySummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(shiftType.getDisplayName());

        if (isOvertimeAllowed != null && isOvertimeAllowed) {
            sb.append(" | Overtime");
        }
        if (isWfhAllowed != null && isWfhAllowed) {
            sb.append(" | WFH");
        }
        if (hasHolidayOverride()) {
            sb.append(" | Holiday");
        }

        return sb.toString();
    }

    /**
     * Check if this shift pattern has any holiday override enabled
     */
    public boolean hasHolidayOverride() {
        return Boolean.TRUE.equals(overrideNationalHoliday)
                || Boolean.TRUE.equals(overrideCompanyHoliday)
                || Boolean.TRUE.equals(overrideJointLeave)
                || Boolean.TRUE.equals(overrideWeeklyLeave);
    }

    /**
     * Get holiday override display string
     */
    public String getHolidayOverrideDisplay() {
        if (!hasHolidayOverride()) {
            return "-";
        }

        StringBuilder sb = new StringBuilder();
        if (Boolean.TRUE.equals(overrideNationalHoliday)) {
            sb.append("Nasional ");
        }
        if (Boolean.TRUE.equals(overrideCompanyHoliday)) {
            sb.append("Perusahaan ");
        }
        if (Boolean.TRUE.equals(overrideJointLeave)) {
            sb.append("Cuti Bersama ");
        }
        if (Boolean.TRUE.equals(overrideWeeklyLeave)) {
            sb.append("Mingguan ");
        }

        return sb.toString().trim();
    }
}
