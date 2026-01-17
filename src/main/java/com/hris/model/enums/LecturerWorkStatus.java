package com.hris.model.enums;

import lombok.Getter;

/**
 * Lecturer Work Status Enum
 * Status kerja dosen (Aktif, Cuti, Pensiun)
 */
@Getter
public enum LecturerWorkStatus {
    ACTIVE("Aktif"),
    LEAVE("Cuti"),
    RETIRED("Pensiun");

    private final String displayName;

    LecturerWorkStatus(String displayName) {
        this.displayName = displayName;
    }
}
