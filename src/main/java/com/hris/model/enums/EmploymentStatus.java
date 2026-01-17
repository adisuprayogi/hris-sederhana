package com.hris.model.enums;

import lombok.Getter;

/**
 * Employment Status Enum (Status Kepegawaian - Sesuai UU Ketenagakerjaan)
 */
@Getter
public enum EmploymentStatus {
    PERMANENT("Karyawan Tetap"),
    CONTRACT("Karyawan Kontrak"),
    PROBATION("Masa Percobaan"),
    DAILY("Harian Lepas");

    private final String displayName;

    EmploymentStatus(String displayName) {
        this.displayName = displayName;
    }
}
