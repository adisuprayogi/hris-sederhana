package com.hris.model.enums;

import lombok.Getter;

/**
 * Gender Restriction Enum for LeaveTypeSetting
 * Menentukan batasan gender untuk jenis cuti tertentu
 */
@Getter
public enum GenderRestriction {
    ALL("Semua", "Berlaku untuk semua karyawan"),
    MALE("Pria", "Hanya untuk karyawan pria"),
    FEMALE("Wanita", "Hanya untuk karyawan wanita");

    private final String displayName;
    private final String description;

    GenderRestriction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
