package com.hris.model.enums;

import lombok.Getter;

/**
 * Company Type Enum
 * Defines the type of institution/organization
 */
@Getter
public enum CompanyType {
    COMPANY("Perusahaan"),
    UNIVERSITY("Universitas"),
    SCHOOL("Sekolah"),
    OTHER("Lainnya");

    private final String displayName;

    CompanyType(String displayName) {
        this.displayName = displayName;
    }
}
