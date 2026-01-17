package com.hris.model.enums;

import lombok.Getter;

/**
 * Gender Enum
 */
@Getter
public enum Gender {
    MALE("Laki-laki"),
    FEMALE("Perempuan");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }
}
