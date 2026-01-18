package com.hris.model;

import com.hris.model.enums.LeaveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Leave Balance Entity
 * Menyimpan saldo cuti karyawan per tahun
 * Fokus pada cuti tahunan yang mengurangi saldo
 */
@Entity
@Table(name = "leave_balances", indexes = {
    @Index(name = "idx_leave_balance_employee", columnList = "employee_id"),
    @Index(name = "idx_leave_balance_year", columnList = "year"),
    @Index(name = "idx_leave_balance_employee_year", columnList = "employee_id,year")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Direct employee ID field for queries
     * Read-only, managed by the employee relationship
     */
    @Column(name = "employee_id", insertable = false, updatable = false)
    private Long employeeId;

    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * Jatah cuti tahunan (default 12 hari)
     */
    @Column(name = "annual_quota", nullable = false)
    private Integer annualQuota = 12;

    /**
     * Saldo cuti yang tersedia
     */
    @Column(name = "balance", nullable = false)
    private Double balance = 0.0;

    /**
     * Saldo cuti yang digunakan
     */
    @Column(name = "used", nullable = false)
    private Double used = 0.0;

    /**
     * Saldo cuti dari tahun sebelumnya yang belum kadaluarsa
     * Cuti tahunan kadaluarsa dalam 6 bulan (PP No. 35 Tahun 2021)
     */
    @Column(name = "carried_forward")
    private Double carriedForward = 0.0;

    /**
     * Tanggal carry forward berlaku
     */
    @Column(name = "carried_forward_expiry_date")
    private LocalDate carriedForwardExpiryDate;

    /**
     * Saldo cuti yang kadaluarsa
     */
    @Column(name = "expired_balance")
    private Double expiredBalance = 0.0;

    /**
     * Total potongan cuti (tidak masuk, tanpa alasan)
     */
    @Column(name = "total_deduction")
    private Double totalDeduction = 0.0;

    /**
     * Keterangan tambahan
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Check if employee has sufficient balance for leave request
     */
    public boolean hasSufficientBalance(double requestedDays) {
        return balance >= requestedDays;
    }

    /**
     * Get remaining balance
     */
    public double getRemainingBalance() {
        return balance - used;
    }

    /**
     * Get total available balance (including carried forward)
     */
    public double getTotalAvailableBalance() {
        return getRemainingBalance() + (carriedForward != null ? carriedForward : 0.0);
    }

    /**
     * Deduct balance for leave request
     */
    public void deductBalance(double days) {
        if (balance >= days) {
            balance -= days;
            used += days;
        } else {
            throw new IllegalArgumentException("Insufficient leave balance");
        }
    }

    /**
     * Add balance (manual adjustment or reimbursement)
     */
    public void addBalance(double days) {
        balance += days;
    }

    /**
     * Check if carried forward balance has expired
     */
    public boolean isCarriedForwardExpired() {
        if (carriedForwardExpiryDate == null || carriedForward == null) {
            return false;
        }
        return LocalDate.now().isAfter(carriedForwardExpiryDate);
    }

    /**
     * Expire carried forward balance
     * Call this when the expiry date has passed
     */
    public void expireCarriedForward() {
        if (isCarriedForwardExpired() && carriedForward > 0) {
            expiredBalance += carriedForward;
            carriedForward = 0.0;
        }
    }

    /**
     * Get balance utilization percentage
     */
    public double getUtilizationPercentage() {
        if (annualQuota == 0) {
            return 0.0;
        }
        return (used / annualQuota) * 100;
    }

    /**
     * Reset balance for new year
     * Carry forward unused balance from previous year
     * Expired balance is calculated based on 6-month rule
     */
    public void resetForNewYear() {
        double unused = balance - used;

        // Calculate expired balance (cutoff after 6 months)
        // According to PP No. 35 Tahun 2021, leave expires after 6 months into the next year
        // So at most 6 days can be carried forward (if employee had 12 days quota)
        double maxCarryForward = annualQuota / 2; // Half of annual quota
        double actualCarryForward = Math.min(unused, maxCarryForward);

        this.carriedForward = actualCarryForward;
        this.carriedForwardExpiryDate = LocalDate.now().plusMonths(6);

        // Calculate expired balance
        this.expiredBalance = Math.max(0, unused - maxCarryForward);

        // Reset balance for new year
        this.balance = (double) annualQuota;
        this.used = 0.0;
    }

    /**
     * Get display name with year
     */
    public String getDisplayNameWithYear() {
        return employee != null ? employee.getFullName() + " (" + year + ")" : "Unknown (" + year + ")";
    }
}
