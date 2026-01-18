package com.hris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Employee Shift Schedule Entity
 * Override Shift per Tanggal untuk Employee
 */
@Entity
@Table(name = "employee_shift_schedules")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeShiftSchedule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "working_hours_id")
    private Long workingHoursId;

    @Column(name = "override_is_wfh")
    private Boolean overrideIsWfh;

    @Column(name = "override_is_overtime_allowed")
    private Boolean overrideIsOvertimeAllowed;

    @Column(name = "override_attendance_mandatory")
    private Boolean overrideAttendanceMandatory;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "working_hours_id", insertable = false, updatable = false)
    private WorkingHours workingHours;

    /**
     * Check if this is OFF (libur)
     */
    public boolean isOff() {
        return workingHoursId == null;
    }

    /**
     * Check if this schedule has any overrides
     */
    public boolean hasOverrides() {
        return overrideIsWfh != null ||
               overrideIsOvertimeAllowed != null ||
               overrideAttendanceMandatory != null;
    }

    /**
     * Check if this schedule is in the past
     */
    public boolean isPast() {
        return scheduleDate.isBefore(LocalDate.now());
    }

    /**
     * Check if this schedule is today
     */
    public boolean isToday() {
        return scheduleDate.equals(LocalDate.now());
    }

    /**
     * Check if this schedule is in the future
     */
    public boolean isFuture() {
        return scheduleDate.isAfter(LocalDate.now());
    }
}
