package com.hris.model.enums;

/**
 * Payroll Period Type Enum
 * Defines different payroll period types for employees and lecturers
 */
public enum PayrollPeriodType {
    /**
     * Monthly payroll period
     * Payroll calculated once per month
     * Cut-off date: 25th of each month
     * Payment date: 1st of next month
     */
    MONTHLY("Bulanan", "Dihitung sekali sebulan, dibayar tanggal 1 bulan berikutnya"),

    /**
     * Hourly payroll period
     * Based on actual working/teaching hours
     * Commonly used for contract lecturers
     */
    HOURLY("Per Jam", "Dihitung berdasarkan jam kerja/mengajar aktual"),

    /**
     * Weekly payroll period
     * Calculated once per week
     */
    WEEKLY("Mingguan", "Dihitung sekali seminggu"),

    /**
     * Bi-weekly (every 2 weeks) payroll period
     */
    BI_WEEKLY("Dua Mingguan", "Dihitung setiap dua minggu sekali"),

    /**
     * Semester-based payroll period
     * Common for lecturers with semester contracts
     * Calculated based on semester teaching load
     */
    SEMESTER("Per Semester", "Dihitung berdasarkan beban mengajar satu semester"),

    /**
     * Daily payroll period
     */
    DAILY("Harian", "Dihitung harian");

    private final String displayName;
    private final String description;

    PayrollPeriodType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
