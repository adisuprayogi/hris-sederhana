package com.hris.model.enums;

import lombok.Getter;

/**
 * Role Type Enum
 * Defines the available roles in the system
 */
@Getter
public enum RoleType {
    ADMIN("Administrator"),
    HR("HR Staff"),
    EMPLOYEE("Employee"),
    DOSEN("Lecturer");

    private final String displayName;

    RoleType(String displayName) {
        this.displayName = displayName;
    }
}
