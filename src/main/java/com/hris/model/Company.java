package com.hris.model;

import com.hris.model.enums.CompanyType;
import com.hris.model.enums.PayrollPeriodType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Company Entity
 * Represents the institution/organization (singleton - only one record should exist)
 */
@Entity
@Table(name = "companies")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Company extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // DATA PERUSAHAAN/INSTITUSI
    // =====================================================

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CompanyType type = CompanyType.UNIVERSITY;

    // =====================================================
    // ALAMAT & KONTAK
    // =====================================================

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "website")
    private String website;

    // =====================================================
    // DATA LEGAL
    // =====================================================

    @Column(name = "npwp_company", unique = true, length = 25)
    private String npwpCompany;

    @Column(name = "siup_number", length = 50)
    private String siupNumber;

    @Column(name = "siup_expired_date")
    private LocalDate siupExpiredDate;

    @Column(name = "establishment_date")
    private LocalDate establishmentDate;

    // =====================================================
    // DATA BPJS PERUSAHAAN
    // =====================================================

    @Column(name = "bpjs_ketenagakerjaan_no", length = 30)
    private String bpjsKetenagakerjaanNo;

    @Column(name = "bpjs_kesehatan_no", length = 30)
    private String bpjsKesehatanNo;

    // =====================================================
    // DATA KEUANGAN
    // =====================================================

    @Column(name = "tax_address", columnDefinition = "TEXT")
    private String taxAddress;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_name", length = 100)
    private String bankAccountName;

    // =====================================================
    // BRANDING
    // =====================================================

    @Column(name = "logo_path")
    private String logoPath;

    @Column(name = "stamp_path")
    private String stampPath;

    // =====================================================
    // KONFIGURASI JAM KERJA
    // =====================================================

    @Column(name = "working_days", length = 50)
    private String workingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";

    @Column(name = "clock_in_start")
    private LocalTime clockInStart = LocalTime.of(8, 0);

    @Column(name = "clock_in_end")
    private LocalTime clockInEnd = LocalTime.of(9, 0);

    @Column(name = "clock_out_start")
    private LocalTime clockOutStart = LocalTime.of(17, 0);

    @Column(name = "clock_out_end")
    private LocalTime clockOutEnd = LocalTime.of(18, 0);

    // =====================================================
    // KONFIGURASI PAYROLL PERIOD
    // =====================================================

    /**
     * Payroll period type for regular employees
     * MONTHLY: Payroll calculated once per month
     * Converted using PayrollPeriodTypeConverter (VARCHAR storage)
     */
    @Column(name = "employee_payroll_period", length = 20)
    private PayrollPeriodType employeePayrollPeriod = PayrollPeriodType.MONTHLY;

    /**
     * Payroll cut-off date for employees (1-31)
     * Example: 25 means payroll calculated on 25th for current month
     */
    @Column(name = "employee_payroll_cutoff_date")
    private Integer employeePayrollCutoffDate = 25;

    /**
     * Payroll payment date for employees (1-31)
     * Example: 1 means salary is paid on 1st of next month
     */
    @Column(name = "employee_payroll_payment_date")
    private Integer employeePayrollPaymentDate = 1;

    /**
     * Payroll period type for permanent lecturers (DOSEN_TETAP)
     * MONTHLY: Payroll calculated once per month
     * Converted using PayrollPeriodTypeConverter (VARCHAR storage)
     */
    @Column(name = "permanent_lecturer_payroll_period", length = 20)
    private PayrollPeriodType permanentLecturerPayrollPeriod = PayrollPeriodType.MONTHLY;

    /**
     * Payroll cut-off date for permanent lecturers (1-31)
     * Example: 25 means payroll calculated on 25th for current month
     */
    @Column(name = "permanent_lecturer_payroll_cutoff_date")
    private Integer permanentLecturerPayrollCutoffDate = 25;

    /**
     * Payroll payment date for permanent lecturers (1-31)
     * Example: 1 means salary is paid on 1st of next month
     */
    @Column(name = "permanent_lecturer_payroll_payment_date")
    private Integer permanentLecturerPayrollPaymentDate = 1;

    /**
     * Payroll period type for contract lecturers (DOSEN_TIDAK_TETAP)
     * HOURLY: Based on teaching hours (credit hours)
     * SEMESTER: Based on semester teaching load
     * MONTHLY: Monthly with prorated calculation
     * Converted using PayrollPeriodTypeConverter (VARCHAR storage)
     */
    @Column(name = "contract_lecturer_payroll_period", length = 20)
    private PayrollPeriodType contractLecturerPayrollPeriod = PayrollPeriodType.HOURLY;

    /**
     * Payroll cut-off date for contract lecturers (1-31)
     * Example: 25 means payroll calculated on 25th for current month
     */
    @Column(name = "contract_lecturer_payroll_cutoff_date")
    private Integer contractLecturerPayrollCutoffDate = 25;

    /**
     * Payroll payment date for contract lecturers (1-31)
     * Example: 1 means salary is paid on 1st of next month
     */
    @Column(name = "contract_lecturer_payroll_payment_date")
    private Integer contractLecturerPayrollPaymentDate = 1;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Get working days as list of DayOfWeek
     */
    public List<DayOfWeek> getWorkingDaysList() {
        if (workingDays == null || workingDays.isEmpty()) {
            return Arrays.asList(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            );
        }
        return Arrays.stream(workingDays.split(","))
            .map(String::trim)
            .map(DayOfWeek::valueOf)
            .collect(Collectors.toList());
    }

    /**
     * Set working days from list of DayOfWeek
     */
    public void setWorkingDaysList(List<DayOfWeek> days) {
        this.workingDays = days.stream()
            .map(DayOfWeek::name)
            .collect(Collectors.joining(","));
    }

    /**
     * Check if a specific day is a working day
     */
    public boolean isWorkingDay(DayOfWeek day) {
        return getWorkingDaysList().contains(day);
    }

    /**
     * Get formatted working hours for display
     */
    public String getWorkingHoursDisplay() {
        return String.format("%s - %s",
            clockInStart != null ? clockInStart.toString() : "08:00",
            clockOutEnd != null ? clockOutEnd.toString() : "18:00"
        );
    }

    /**
     * Get full address as single line
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isEmpty()) {
            sb.append(address);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (province != null && !province.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(province);
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(postalCode);
        }
        return sb.toString();
    }
}
