package com.hris.model;

import com.hris.model.enums.GenderRestriction;
import com.hris.model.enums.LeaveTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity for Leave Type Settings
 * Menyimpan pengaturan global untuk jenis-jenis cuti yang tersedia
 */
@Entity
@Table(name = "leave_types")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LeaveTypeSetting extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveTypeEnum leaveType = LeaveTypeEnum.QUOTA;

    @Column(name = "annual_quota")
    private Integer annualQuota;

    // Carry Forward Settings
    @Column(name = "allow_carry_forward")
    private Boolean allowCarryForward = false;

    @Column(name = "max_carry_forward_days")
    private Integer maxCarryForwardDays;

    @Column(name = "carry_forward_expiry_month")
    private Integer carryForwardExpiryMonth;

    @Column(name = "carry_forward_expiry_day")
    private Integer carryForwardExpiryDay;

    // Employee Criteria
    @Column(name = "min_years_of_service")
    private Integer minYearsOfService = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_restriction")
    private GenderRestriction genderRestriction = GenderRestriction.ALL;

    @Column(name = "is_paid")
    private Boolean isPaid = true;

    @Column(name = "require_proof")
    private Boolean requireProof = false;

    @Column(name = "proof_description")
    private String proofDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Helper Methods

    /**
     * Check if this leave type is quota-based
     */
    public boolean isQuotaBased() {
        return leaveType == LeaveTypeEnum.QUOTA;
    }

    /**
     * Check if this leave type allows carry forward
     */
    public boolean hasCarryForward() {
        return allowCarryForward != null && allowCarryForward
                && maxCarryForwardDays != null
                && carryForwardExpiryMonth != null
                && carryForwardExpiryDay != null;
    }

    /**
     * Get formatted expiry date for carry forward
     */
    public String getCarryForwardExpiryFormatted() {
        if (!hasCarryForward()) {
            return "-";
        }
        return carryForwardExpiryDay + " " + getMonthName(carryForwardExpiryMonth);
    }

    /**
     * Get month name in Indonesian
     */
    private String getMonthName(int month) {
        String[] months = {"", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                          "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        return month >= 1 && month <= 12 ? months[month] : "-";
    }

    /**
     * Check if employee is eligible based on years of service
     */
    public boolean isEligible(Integer employeeYearsOfService) {
        return employeeYearsOfService != null
                && employeeYearsOfService >= (minYearsOfService != null ? minYearsOfService : 0);
    }

    /**
     * Check if gender is eligible
     */
    public boolean isEligible(GenderRestriction gender) {
        if (genderRestriction == GenderRestriction.ALL) {
            return true;
        }
        return genderRestriction == gender;
    }

    /**
     * Get display text for quota
     */
    public String getQuotaDisplay() {
        if (leaveType == LeaveTypeEnum.NO_QUOTA) {
            return "Unlimited";
        }
        return annualQuota != null ? annualQuota + " hari" : "0 hari";
    }

    /**
     * Get display text for min years of service
     */
    public String getMinYearsDisplay() {
        if (minYearsOfService == null || minYearsOfService == 0) {
            return "Semua";
        }
        return minYearsOfService + " tahun";
    }

    /**
     * Get display text for gender restriction
     */
    public String getGenderRestrictionDisplay() {
        return switch (genderRestriction) {
            case MALE -> "Pria";
            case FEMALE -> "Wanita";
            case ALL -> "Semua";
        };
    }
}
