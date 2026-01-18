package com.hris.model.enums;

import lombok.Getter;

/**
 * Holiday Type Enum
 * Menentukan jenis hari libur
 */
@Getter
public enum HolidayType {
    NATIONAL("Hari Libur Nasional", "Libur nasional berdasarkan kalender pemerintah"),
    COMPANY("Hari Libur Perusahaan", "Hari libur khusus perusahaan"),
    COLLECTIVE_LEAVE("Cuti Bersama", "Cuti bersama yang ditetapkan perusahaan");

    private final String displayName;
    private final String description;

    HolidayType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isNational() {
        return this == NATIONAL;
    }

    public boolean isCompany() {
        return this == COMPANY;
    }

    public boolean isCollectiveLeave() {
        return this == COLLECTIVE_LEAVE;
    }
}
