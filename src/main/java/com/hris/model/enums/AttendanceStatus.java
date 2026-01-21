package com.hris.model.enums;

import lombok.Getter;

/**
 * Attendance Status Enum
 */
@Getter
public enum AttendanceStatus {
    PRESENT("Hadir"),
    LATE("Terlambat"),
    LEAVE("Izin"),
    SICK("Sakit"),
    ABSENT("Alpha"),
    EARLY_LEAVE("Pulang Awal"),
    WFH("WFH");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }
}
