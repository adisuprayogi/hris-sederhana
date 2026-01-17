package com.hris.model.enums;

import lombok.Getter;

/**
 * Leave Type Enum
 * Kategori cuti
 */
@Getter
public enum LeaveType {
    ANNUAL("Cuti Tahunan"),
    SICK("Cuti Sakit"),
    MATERNITY("Cuti Melahirkan"),
    MARRIAGE("Cuti Menikah"),
    SPECIAL("Cuti Khusus"),
    UNPAID("Cuti Tanpa Gaji");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }
}
