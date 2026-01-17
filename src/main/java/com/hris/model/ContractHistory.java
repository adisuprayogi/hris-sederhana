package com.hris.model;

import com.hris.model.enums.EmploymentStatus;
import com.hris.model.enums.EmploymentStatusChange;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Model untuk menyimpan riwayat perubahan status kerja dan periode kontrak karyawan
 */
@Entity
@Table(name = "contract_history")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ContractHistory extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Karyawan terkait
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Jenis perubahan status kerja
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, columnDefinition = "VARCHAR(50)")
    private EmploymentStatusChange changeType;

    /**
     * Status kerja sebelumnya
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", columnDefinition = "VARCHAR(20)")
    private EmploymentStatus oldStatus;

    /**
     * Status kerja baru
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, columnDefinition = "VARCHAR(20)")
    private EmploymentStatus newStatus;

    /**
     * Tanggal mulai periode/status ini
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Tanggal selesai periode/status ini
     * NULL jika status masih berlaku (status saat ini)
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Tanggal pengangkatan sebagai karyawan tetap
     * Diisi hanya jika new_status == PERMANENT
     */
    @Column(name = "permanent_appointment_date")
    private LocalDate permanentAppointmentDate;

    /**
     * Nomor kontrak (opsional, untuk karyawan kontrak)
     */
    @Column(name = "contract_number")
    private String contractNumber;

    /**
     * Alasan perubahan status
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /**
     * Catatan tambahan
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Path ke file dokumen (SK/PKWT)
     */
    @Column(name = "document_path")
    private String documentPath;

    // Helper methods

    /**
     * Cek apakah ini adalah periode kontrak
     */
    public boolean isContractPeriod() {
        return newStatus == EmploymentStatus.CONTRACT;
    }

    /**
     * Cek apakah ini adalah pengangkatan karyawan tetap
     */
    public boolean isPermanentAppointment() {
        return newStatus == EmploymentStatus.PERMANENT
            && (changeType == EmploymentStatusChange.PROBATION_TO_PERMANENT
                || changeType == EmploymentStatusChange.CONTRACT_TO_PERMANENT);
    }

    /**
     * Cek apakah ini adalah perpanjangan kontrak
     */
    public boolean isContractRenewal() {
        return changeType == EmploymentStatusChange.CONTRACT_RENEWAL;
    }

    /**
     * Cek apakah periode ini sudah berakhir
     */
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    /**
     * Cek apakah ini adalah status aktif saat ini
     */
    public boolean isCurrent() {
        return endDate == null;
    }

    /**
     * Hitung durasi periode dalam hari
     */
    public long getDurationInDays() {
        if (endDate == null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, LocalDate.now());
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Hitung durasi periode dalam bulan
     */
    public long getDurationInMonths() {
        if (endDate == null) {
            return java.time.temporal.ChronoUnit.MONTHS.between(startDate, LocalDate.now());
        }
        return java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate);
    }
}
