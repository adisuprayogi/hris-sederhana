package com.hris.model.enums;

import lombok.Getter;

/**
 * Leave Type Enum
 * Kategori cuti sesuai UU No. 13 Tahun 2003 tentang Ketenagakerjaan
 */
@Getter
public enum LeaveType {
    ANNUAL("Cuti Tahunan", "Cuti tahunan yang diambil dari jatah cuti (12 hari/tahun)", true, 12),
    SICK("Cuti Sakit", "Cuti sakit dengan surat dokter", false, 0),
    MATERNITY("Cuti Melahirkan", "Cuti melahirkan 3 bulan sebelum + 1.5 bulan setelah kelahiran", false, 0),
    MARRIAGE("Cuti Menikah", "Cuti menikah untuk karyawan (3 hari) atau anak karyawan (2 hari)", false, 3),
    RELIGIOUS("Cuti Ibadah", "Cuti untuk keperluan ibadah (haji, baptis, dll)", false, 0),
    SPECIAL_FAMILY("Cuti Keluarga", "Cuti untuk kepentingan keluarga (kematian, khitanan, dll)", false, 2),
    SPECIAL("Cuti Khusus", "Cuti khusus dengan alasan tertentu", false, 0),
    UNPAID("Cuti Tanpa Gaji", "Cuti tanpa gaji dengan izin perusahaan", false, 0),
    COLLECTIVE("Cuti Bersama", "Cuti bersama yang ditetapkan pemerintah/perusahaan", false, 0);

    private final String displayName;
    private final String description;
    private final boolean deductsFromBalance;
    private final int defaultQuotaDays;

    LeaveType(String displayName, String description, boolean deductsFromBalance, int defaultQuotaDays) {
        this.displayName = displayName;
        this.description = description;
        this.deductsFromBalance = deductsFromBalance;
        this.defaultQuotaDays = defaultQuotaDays;
    }

    /**
     * Check if this leave type deducts from annual leave balance
     */
    public boolean isDeductingFromBalance() {
        return deductsFromBalance;
    }

    /**
     * Get default quota for this leave type
     * Only applicable for leave types with fixed quota (like ANNUAL, MARRIAGE)
     */
    public int getDefaultQuota() {
        return defaultQuotaDays;
    }

    /**
     * Check if medical certificate is required
     */
    public boolean requiresMedicalCertificate() {
        return this == SICK || this == MATERNITY;
    }

    /**
     * Check if this is a paid leave
     */
    public boolean isPaidLeave() {
        return this != UNPAID;
    }
}
