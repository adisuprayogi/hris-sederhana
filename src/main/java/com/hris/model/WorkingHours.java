package com.hris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Working Hours Entity
 * Master Jam Kerja - Layer 1 dari Shift System
 */
@Entity
@Table(name = "working_hours")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHours extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "start_time")
    private java.time.LocalTime startTime;

    @Column(name = "end_time")
    private java.time.LocalTime endTime;

    @Column(name = "is_overnight")
    private Boolean isOvernight = false;

    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes = 60;

    @Column(name = "required_work_hours", precision = 4, scale = 2)
    private java.math.BigDecimal requiredWorkHours = java.math.BigDecimal.valueOf(8.00);

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "color", length = 20)
    private String color = "#3B82F6";

    /**
     * Check if this is OFF (libur)
     */
    public boolean isOff() {
        return "WH_OFF".equals(code) || startTime == null;
    }

    /**
     * Check if this is Flexible
     */
    public boolean isFlexible() {
        return "WH_FLEX".equals(code);
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        if (isOff()) {
            return "OFF";
        }
        if (isFlexible()) {
            return "Flexible";
        }
        if (startTime != null && endTime != null) {
            return String.format("%s - %s", startTime, endTime);
        }
        return name;
    }

    /**
     * Get work duration in minutes
     */
    public Integer getWorkDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }

        long startMinutes = startTime.getHour() * 60 + startTime.getMinute();
        long endMinutes = endTime.getHour() * 60 + endTime.getMinute();

        if (isOvernight) {
            // Shift malam: 22:00 - 06:00 (next day)
            return (int) ((24 * 60 - startMinutes) + endMinutes);
        } else {
            return (int) (endMinutes - startMinutes);
        }
    }

    /**
     * Get net work duration (minus break)
     */
    public Integer getNetWorkDurationMinutes() {
        return Math.max(0, getWorkDurationMinutes() - (breakDurationMinutes != null ? breakDurationMinutes : 0));
    }
}
