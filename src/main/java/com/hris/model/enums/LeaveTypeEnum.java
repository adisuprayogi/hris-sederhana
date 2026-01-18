package com.hris.model.enums;

import lombok.Getter;

/**
 * Leave Type Enum for LeaveTypeSetting
 * Menentukan apakah jenis cuti berkuota atau tidak
 */
@Getter
public enum LeaveTypeEnum {
    QUOTA("Berkuota", "Cuti dengan jatah tertentu per tahun"),
    NO_QUOTA("Tidak Berkuota", "Cuti tanpa batas jatah (unlimited)");

    private final String displayName;
    private final String description;

    LeaveTypeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
