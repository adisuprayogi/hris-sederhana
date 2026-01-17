package com.hris.model.enums;

import lombok.Getter;

/**
 * Marital Status Enum (Status Pernikahan - Untuk Pajak & BPJS)
 */
@Getter
public enum MaritalStatus {
    SINGLE("Belum Menikah"),
    MARRIED("Menikah"),
    DIVORCED("Cerai Hidup"),
    WIDOWED("Cerai Mati");

    private final String displayName;

    MaritalStatus(String displayName) {
        this.displayName = displayName;
    }
}
