package com.hris.model.enums;

import lombok.Getter;

/**
 * Shift Type Enum
 */
@Getter
public enum ShiftType {
    FIXED("Fixed"),
    FLEXIBLE("Flexible"),
    ROTATING("Rotating");

    private final String displayName;

    ShiftType(String displayName) {
        this.displayName = displayName;
    }
}
