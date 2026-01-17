package com.hris.model.enums;

import lombok.Getter;

/**
 * Activity Type Enum
 * Jenis aktivitas user untuk audit trail
 */
@Getter
public enum ActivityType {
    // Authentication
    LOGIN("Login"),
    LOGOUT("Logout"),
    ROLE_SELECTION("Pilih Role"),

    // CRUD Operations
    CREATE("Buat Data"),
    READ("Lihat Data"),
    UPDATE("Update Data"),
    DELETE("Hapus Data"),
    RESTORE("Kembalikan Data"),

    // Business Operations
    CLOCK_IN("Clock In"),
    CLOCK_OUT("Clock Out"),
    SUBMIT_LEAVE("Ajukan Cuti"),
    APPROVE_LEAVE("Setujui Cuti"),
    REJECT_LEAVE("Tolak Cuti"),
    GENERATE_PAYROLL("Generate Gaji"),

    // Data Access
    VIEW_SENSITIVE_DATA("Lihat Data Sensitif");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }
}
