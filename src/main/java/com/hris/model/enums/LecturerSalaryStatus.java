package com.hris.model.enums;

/**
 * Lecturer Salary Processing Status
 */
public enum LecturerSalaryStatus {
    DRAFT("Draft - Belum dihitung"),
    CALCULATED("Dihitung - Siap dibayar"),
    PAID("Dibayar");

    private final String displayName;

    LecturerSalaryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
