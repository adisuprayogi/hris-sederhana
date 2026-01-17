package com.hris.model.enums;

import lombok.Getter;

/**
 * Payroll Status Enum
 */
@Getter
public enum PayrollStatus {
    DRAFT("Draft"),
    PAID("Sudah Dibayar");

    private final String displayName;

    PayrollStatus(String displayName) {
        this.displayName = displayName;
    }
}
