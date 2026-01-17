package com.hris.model.enums;

import lombok.Getter;

/**
 * Employee Status Enum
 */
@Getter
public enum EmployeeStatus {
    ACTIVE("Aktif"),
    INACTIVE("Tidak Aktif"),
    RESIGNED("Mengundurkan Diri"),
    FIRED("Dipecat");

    private final String displayName;

    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }
}
