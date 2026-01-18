package com.hris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

/**
 * Shift Package Entity
 * Paket Shift (Kombinasi Working Hours per Hari) - Layer 2 dari Shift System
 */
@Entity
@Table(name = "shift_packages")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShiftPackage extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sunday_working_hours_id")
    private Long sundayWorkingHoursId;

    @Column(name = "monday_working_hours_id", nullable = false)
    private Long mondayWorkingHoursId;

    @Column(name = "tuesday_working_hours_id", nullable = false)
    private Long tuesdayWorkingHoursId;

    @Column(name = "wednesday_working_hours_id", nullable = false)
    private Long wednesdayWorkingHoursId;

    @Column(name = "thursday_working_hours_id", nullable = false)
    private Long thursdayWorkingHoursId;

    @Column(name = "friday_working_hours_id", nullable = false)
    private Long fridayWorkingHoursId;

    @Column(name = "saturday_working_hours_id")
    private Long saturdayWorkingHoursId;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "color", length = 20)
    private String color = "#3B82F6";

    // Relations (lazy loading for performance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sunday_working_hours_id", insertable = false, updatable = false)
    private WorkingHours sundayWorkingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monday_working_hours_id", insertable = false, updatable = false)
    private WorkingHours mondayWorkingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tuesday_working_hours_id", insertable = false, updatable = false)
    private WorkingHours tuesdayWorkingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wednesday_working_hours_id", insertable = false, updatable = false)
    private WorkingHours wednesdayWorkingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thursday_working_hours_id", insertable = false, updatable = false)
    private WorkingHours thursdayWorkingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friday_working_hours_id", insertable = false, updatable = false)
    private WorkingHours fridayWorkingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saturday_working_hours_id", insertable = false, updatable = false)
    private WorkingHours saturdayWorkingHours;

    /**
     * Get working hours ID by day of week
     */
    public Long getWorkingHoursIdByDay(DayOfWeek day) {
        return switch (day) {
            case SUNDAY -> sundayWorkingHoursId;
            case MONDAY -> mondayWorkingHoursId;
            case TUESDAY -> tuesdayWorkingHoursId;
            case WEDNESDAY -> wednesdayWorkingHoursId;
            case THURSDAY -> thursdayWorkingHoursId;
            case FRIDAY -> fridayWorkingHoursId;
            case SATURDAY -> saturdayWorkingHoursId;
        };
    }

    /**
     * Check if a specific day is a working day (not OFF)
     */
    public boolean isWorkingDay(DayOfWeek day) {
        Long workingHoursId = getWorkingHoursIdByDay(day);
        return workingHoursId != null;
    }

    /**
     * Get working days count per week
     */
    public int getWorkingDaysPerWeek() {
        int count = 0;
        if (mondayWorkingHoursId != null) count++;
        if (tuesdayWorkingHoursId != null) count++;
        if (wednesdayWorkingHoursId != null) count++;
        if (thursdayWorkingHoursId != null) count++;
        if (fridayWorkingHoursId != null) count++;
        if (saturdayWorkingHoursId != null) count++;
        if (sundayWorkingHoursId != null) count++;
        return count;
    }

    /**
     * Get display summary
     */
    public String getDisplaySummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDaySummary(DayOfWeek.MONDAY)).append(", ");
        sb.append(getDaySummary(DayOfWeek.TUESDAY)).append(", ");
        sb.append(getDaySummary(DayOfWeek.WEDNESDAY)).append(", ");
        sb.append(getDaySummary(DayOfWeek.THURSDAY)).append(", ");
        sb.append(getDaySummary(DayOfWeek.FRIDAY));
        if (saturdayWorkingHoursId != null || sundayWorkingHoursId != null) {
            sb.append(", ").append(getDaySummary(DayOfWeek.SATURDAY));
            if (sundayWorkingHoursId != null) {
                sb.append(", ").append(getDaySummary(DayOfWeek.SUNDAY));
            }
        }
        return sb.toString();
    }

    private String getDaySummary(DayOfWeek day) {
        Long whId = getWorkingHoursIdByDay(day);
        if (whId == null) {
            return getDayShortName(day) + ": OFF";
        }
        return getDayShortName(day);
    }

    private String getDayShortName(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Mon";
            case TUESDAY -> "Tue";
            case WEDNESDAY -> "Wed";
            case THURSDAY -> "Thu";
            case FRIDAY -> "Fri";
            case SATURDAY -> "Sat";
            case SUNDAY -> "Sun";
        };
    }
}
