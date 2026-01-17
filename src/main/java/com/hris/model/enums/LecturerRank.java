package com.hris.model.enums;

import lombok.Getter;

/**
 * Lecturer Rank/Jenjang Enum
 * Jenjang karir dosen di Indonesia
 */
@Getter
public enum LecturerRank {
    ASISTEN_AHLI("Asisten Ahli"),
    LEKTOR("Lektor"),
    LEKTOR_KEPALA("Lektor Kepala"),
    PROFESOR("Profesor");

    private final String displayName;

    LecturerRank(String displayName) {
        this.displayName = displayName;
    }
}
