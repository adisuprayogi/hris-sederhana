package com.hris.model.enums;

import lombok.Getter;

/**
 * Attendance Status Enum
 */
@Getter
public enum AttendanceStatus {
    PRESENT("Hadir"),
    LATE("Telat"),
    LEAVE("Izin"),
    SICK("Sakit"),
    ABSENT("Alpha");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }
}
