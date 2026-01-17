package com.hris.model.enums;

import lombok.Getter;

/**
 * Lecturer Employment Status Enum
 * Status kepegawaian dosen (Dosen Tetap atau Dosen Tidak Tetap)
 */
@Getter
public enum LecturerEmploymentStatus {
    DOSEN_TETAP("Dosen Tetap"),
    DOSEN_TIDAK_TETAP("Dosen Tidak Tetap");

    private final String displayName;

    LecturerEmploymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
