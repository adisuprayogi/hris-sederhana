package com.hris.model.enums;

/**
 * Jenis perubahan status kerja
 */
public enum EmploymentStatusChange {
    INITIAL_HIRING("Perekrutan Awal"),
    PROBATION_TO_CONTRACT("Konversi Probation ke Contract"),
    PROBATION_TO_PERMANENT("Pengangkatan Karyawan Tetap (Probation ke Permanent)"),
    CONTRACT_TO_PERMANENT("Pengangkatan Karyawan Tetap (Contract ke Permanent)"),
    CONTRACT_RENEWAL("Perpanjangan Kontrak"),
    CONTRACT_EXPIRED("Kontrak Berakhir"),
    RESIGNATION("Pengunduran Diri"),
    TERMINATION("Pemutusan Kerja"),
    RETIREMENT("Pensiun"),
    STATUS_CHANGE("Perubahan Status Lainnya");

    private final String displayName;

    EmploymentStatusChange(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
