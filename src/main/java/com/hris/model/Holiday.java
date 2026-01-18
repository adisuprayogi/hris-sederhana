package com.hris.model;

import com.hris.model.enums.HolidayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Holiday Entity
 * Menyimpan data hari libur (nasional, perusahaan, cuti bersama)
 */
@Entity
@Table(name = "holidays", indexes = {
    @Index(name = "idx_holiday_date", columnList = "date"),
    @Index(name = "idx_holiday_type", columnList = "holiday_type"),
    @Index(name = "idx_holiday_year", columnList = "year")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Holiday extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private HolidayType holidayType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "repeat_annually", nullable = false)
    private Boolean repeatAnnually = false;

    /**
     * Check if this holiday is active for a specific date
     */
    public boolean isActiveForDate(LocalDate checkDate) {
        return isActive && date.equals(checkDate);
    }

    /**
     * Check if this is a national holiday
     */
    public boolean isNationalHoliday() {
        return holidayType == HolidayType.NATIONAL;
    }

    /**
     * Check if this is a company holiday
     */
    public boolean isCompanyHoliday() {
        return holidayType == HolidayType.COMPANY;
    }

    /**
     * Check if this is collective leave
     */
    public boolean isCollectiveLeave() {
        return holidayType == HolidayType.COLLECTIVE_LEAVE;
    }

    /**
     * Get display name with year
     */
    public String getDisplayNameWithYear() {
        return name + " (" + year + ")";
    }
}
