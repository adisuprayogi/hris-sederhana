package com.hris.model;

import com.hris.model.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Attendance Record Entity
 * Tracks employee daily attendance with clock in/out, late, overtime, and deductions
 *
 * Integrates with Shift Management system:
 * - Uses shift pattern for working hours calculation
 * - Uses shift pattern tolerance for late calculation
 * - Uses shift pattern deduction rules
 */
@Entity
@Table(name = "attendance_records", indexes = {
    @Index(name = "idx_attendance_employee_date", columnList = "employee_id,attendance_date"),
    @Index(name = "idx_attendance_date", columnList = "attendance_date"),
    @Index(name = "idx_attendance_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // EMPLOYEE & DATE
    // =====================================================

    /**
     * Employee
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "employee_id", insertable = false, updatable = false)
    private Long employeeId;

    /**
     * Attendance date
     */
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    // =====================================================
    // CLOCK IN DATA
    // =====================================================

    /**
     * Clock in time
     */
    @Column(name = "clock_in_time")
    private LocalTime clockInTime;

    /**
     * Clock in latitude
     */
    @Column(name = "clock_in_latitude", precision = 10, scale = 8)
    private BigDecimal clockInLatitude;

    /**
     * Clock in longitude
     */
    @Column(name = "clock_in_longitude", precision = 11, scale = 8)
    private BigDecimal clockInLongitude;

    /**
     * Clock in device info
     */
    @Column(name = "clock_in_device_info", length = 255)
    private String clockInDeviceInfo;

    /**
     * Clock in photo path
     */
    @Column(name = "clock_in_photo_path")
    private String clockInPhotoPath;

    // =====================================================
    // CLOCK OUT DATA
    // =====================================================

    /**
     * Clock out time
     */
    @Column(name = "clock_out_time")
    private LocalTime clockOutTime;

    /**
     * Clock out latitude
     */
    @Column(name = "clock_out_latitude", precision = 10, scale = 8)
    private BigDecimal clockOutLatitude;

    /**
     * Clock out longitude
     */
    @Column(name = "clock_out_longitude", precision = 11, scale = 8)
    private BigDecimal clockOutLongitude;

    /**
     * Clock out device info
     */
    @Column(name = "clock_out_device_info", length = 255)
    private String clockOutDeviceInfo;

    /**
     * Clock out photo path
     */
    @Column(name = "clock_out_photo_path")
    private String clockOutPhotoPath;

    // =====================================================
    // SHIFT REFERENCE (SNAPSHOT)
    // =====================================================

    /**
     * Working hours used for this attendance
     * Snapshot reference to ensure data consistency even if working hours changes later
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "working_hours_id")
    private WorkingHours workingHours;

    @Column(name = "working_hours_id", insertable = false, updatable = false)
    private Long workingHoursId;

    /**
     * Shift pattern used for this attendance
     * Snapshot reference for tolerance, deduction rules, etc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_pattern_id")
    private ShiftPattern shiftPattern;

    @Column(name = "shift_pattern_id", insertable = false, updatable = false)
    private Long shiftPatternId;

    // =====================================================
    // LATE CALCULATION
    // =====================================================

    /**
     * Is this attendance late
     */
    @Column(name = "is_late")
    @Builder.Default
    private Boolean isLate = false;

    /**
     * Late duration in minutes
     */
    @Column(name = "late_duration_minutes")
    @Builder.Default
    private Integer lateDurationMinutes = 0;

    /**
     * Late deduction amount (from shift pattern calculation)
     */
    @Column(name = "late_deduction_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal lateDeductionAmount = BigDecimal.ZERO;

    // =====================================================
    // EARLY LEAVE CALCULATION
    // =====================================================

    /**
     * Did employee leave early
     */
    @Column(name = "is_early_leave")
    @Builder.Default
    private Boolean isEarlyLeave = false;

    /**
     * Early leave duration in minutes
     */
    @Column(name = "early_leave_duration_minutes")
    @Builder.Default
    private Integer earlyLeaveDurationMinutes = 0;

    /**
     * Early leave deduction amount
     */
    @Column(name = "early_leave_deduction_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal earlyLeaveDeductionAmount = BigDecimal.ZERO;

    // =====================================================
    // OVERTIME CALCULATION
    // =====================================================

    /**
     * Did employee work overtime
     */
    @Column(name = "is_overtime")
    @Builder.Default
    private Boolean isOvertime = false;

    /**
     * Overtime duration in minutes
     */
    @Column(name = "overtime_duration_minutes")
    @Builder.Default
    private Integer overtimeDurationMinutes = 0;

    // =====================================================
    // WORK DURATION
    // =====================================================

    /**
     * Actual work duration in minutes
     */
    @Column(name = "actual_work_minutes")
    @Builder.Default
    private Integer actualWorkMinutes = 0;

    /**
     * Required work duration in minutes (from working hours)
     */
    @Column(name = "required_work_minutes")
    @Builder.Default
    private Integer requiredWorkMinutes = 0;

    /**
     * Underwork duration in minutes (deficit from required)
     */
    @Column(name = "underwork_minutes")
    @Builder.Default
    private Integer underworkMinutes = 0;

    /**
     * Underwork deduction amount (from shift pattern calculation)
     */
    @Column(name = "underwork_deduction_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal underworkDeductionAmount = BigDecimal.ZERO;

    // =====================================================
    // STATUS & FLAGS
    // =====================================================

    /**
     * Attendance status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    /**
     * Is this WFH (Work From Home)
     */
    @Column(name = "is_wfh")
    @Builder.Default
    private Boolean isWfh = false;

    /**
     * Additional notes
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if attendance record is complete (has both clock in and out)
     */
    public boolean isComplete() {
        return clockInTime != null && clockOutTime != null;
    }

    /**
     * Check if only clocked in (not yet clocked out)
     */
    public boolean isClockedInOnly() {
        return clockInTime != null && clockOutTime == null;
    }

    /**
     * Get work duration in human readable format
     */
    public String getWorkDurationDisplay() {
        if (actualWorkMinutes == null || actualWorkMinutes == 0) {
            return "-";
        }
        int hours = actualWorkMinutes / 60;
        int minutes = actualWorkMinutes % 60;
        return String.format("%d jam %d menit", hours, minutes);
    }

    /**
     * Calculate work duration based on clock in and clock out
     * Considers overnight shift
     */
    public void calculateWorkDuration() {
        if (clockInTime == null || clockOutTime == null) {
            return;
        }

        // Check if overnight shift
        boolean isOvernight = shiftPattern != null && workingHours != null
                && workingHours.getIsOvernight() != null
                && workingHours.getIsOvernight();

        int duration;
        if (isOvernight && clockOutTime.isBefore(clockInTime)) {
            // Overnight: 22:00 to 06:00 (next day)
            int toMidnight = 24 * 60 - clockInTime.toSecondOfDay() / 60;
            int fromMidnight = clockOutTime.toSecondOfDay() / 60;
            duration = toMidnight + fromMidnight;
        } else {
            // Normal same-day calculation
            duration = (int) java.time.temporal.ChronoUnit.MINUTES.between(clockInTime, clockOutTime);
        }

        this.actualWorkMinutes = duration;
    }
}
