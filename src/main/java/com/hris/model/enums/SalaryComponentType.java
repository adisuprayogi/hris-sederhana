package com.hris.model.enums;

/**
 * Salary Component Types for Lecturer Salary Details
 */
public enum SalaryComponentType {
    BASIC_SALARY("Gaji Pokok"),
    FUNCTIONAL_ALLOWANCE("Tunjangan Fungsional"),
    TEACHING_OFFLINE("Honor Mengajar Offline"),
    TEACHING_ONLINE("Honor Mengajar Online"),
    GUIDANCE_OFFLINE("Honor Bimbingan Offline"),
    GUIDANCE_ONLINE("Honor Bimbingan Online"),
    EXAMINATION_OFFLINE("Honor Menguji Offline"),
    EXAMINATION_ONLINE("Honor Menguji Online"),
    RESEARCH("Honor Penelitian"),
    PUBLICATION("Honor Publikasi"),
    OTHER("Lain-lain");

    private final String displayName;

    SalaryComponentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
