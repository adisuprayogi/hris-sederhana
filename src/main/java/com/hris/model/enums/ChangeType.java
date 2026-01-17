package com.hris.model.enums;

/**
 * Employee Job Change Type Enum
 * Represents the type of change in an employee's job history
 */
public enum ChangeType {
    /**
     * Initial hiring
     */
    INITIAL("Perekrutan Awal"),

    /**
     * Employee promoted to higher position
     */
    PROMOTION("Kenaikan Jabatan"),

    /**
     * Employee demoted to lower position
     */
    DEMOTION("Penurunan Jabatan"),

    /**
     * Employee transferred to another department
     */
    TRANSFER("Mutasi"),

    /**
     * Employee resigned
     */
    RESIGNATION("Pengunduran Diri"),

    /**
     * Contract renewed
     */
    RENEWAL("Perpanjangan Kontrak"),

    /**
     * Status changed (permanent to contract, etc)
     */
    STATUS_CHANGE("Perubahan Status");

    private final String displayName;

    ChangeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
