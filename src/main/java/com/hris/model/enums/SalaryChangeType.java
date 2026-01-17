package com.hris.model.enums;

/**
 * Salary Change Type Enum
 * Represents the type of salary change
 */
public enum SalaryChangeType {
    /**
     * Initial salary for new hire
     */
    INITIAL("Gaji Awal"),

    /**
     * Salary increase
     */
    INCREASE("Kenaikan Gaji"),

    /**
     * Salary decrease
     */
    DECREASE("Penurunan Gaji"),

    /**
     * Promotion salary adjustment
     */
    PROMOTION("Kenaikan Jabatan"),

    /**
     * Demotion salary adjustment
     */
    DEMOTION("Penurunan Jabatan"),

    /**
     * Annual review adjustment
     */
    ANNUAL_REVIEW("Review Tahunan"),

    /**
     * General adjustment (e.g., inflation adjustment)
     */
    ADJUSTMENT("Penyesuaian"),

    /**
     * Contract renewal adjustment
     */
    RENEWAL("Perpanjangan Kontrak");

    private final String displayName;

    SalaryChangeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
