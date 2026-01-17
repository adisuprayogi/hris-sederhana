package com.hris.model;

import com.hris.model.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Employee Entity
 * Represents an employee in the system
 */
@Entity
@Table(name = "employees")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === DATA IDENTITAS (Per UU Ketenagakerjaan & BPJS) ===
    @Column(name = "nik", unique = true, nullable = false, length = 20)
    private String nik;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "place_of_birth", length = 50)
    private String placeOfBirth;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "mothers_name", length = 100)
    private String mothersName;

    // === ALAMAT & KONTAK ===
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    // === DATA KEPEGAWAIAN (Per Pasal 185 UU 13/2003) ===
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 20)
    private EmploymentStatus employmentStatus = EmploymentStatus.PERMANENT;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "work_location", length = 100)
    private String workLocation;

    // === DATA BPJS (Wajib per Peraturan Pemerintah) ===
    @Column(name = "bpjs_ketenagakerjaan_no", unique = true, length = 20)
    private String bpjsKetenagakerjaanNo;

    @Column(name = "bpjs_kesehatan_no", unique = true, length = 20)
    private String bpjsKesehatanNo;

    @Column(name = "npwp", unique = true, length = 20)
    private String npwp;

    // === DATA GAJI (Untuk BPJS & Payroll) ===
    @Column(name = "basic_salary", precision = 15, scale = 2)
    private BigDecimal basicSalary;

    // === DATA ORGANISASI ===
    @JoinColumn(name = "department_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    @JoinColumn(name = "position_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Position position;

    // =====================================================
    // APPROVAL STRUCTURE FIELDS
    // =====================================================

    /**
     * Approver for this employee (backup approval chain)
     * Used when department head approval is not sufficient or available
     */
    @JoinColumn(name = "approver_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee approver;

    /**
     * Subordinates of this employee (inverse relationship)
     * Employees who have this employee as their approver
     */
    @OneToMany(mappedBy = "approver", fetch = FetchType.LAZY)
    private List<Employee> subordinates = new ArrayList<>();

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if this employee is a department head
     *
     * @return true if this employee is the head of their department
     */
    public boolean isDepartmentHead() {
        return this.department != null && this.department.getHead() != null
                && this.department.getHead().getId().equals(this.id);
    }

    /**
     * Check if this employee is at least a manager level
     *
     * @return true if position level is 4 or higher
     */
    public boolean isManagerOrAbove() {
        return this.position != null && this.position.isAtLeast(4);
    }

    /**
     * Get the approval chain for this employee
     * Returns list of potential approvers from immediate to highest
     *
     * @return List of potential approvers
     */
    public List<Employee> getApprovalChain() {
        List<Employee> chain = new ArrayList<>();

        // 1. Direct approver if set
        if (this.approver != null && !this.approver.getId().equals(this.id)) {
            chain.add(this.approver);
        }

        // 2. Department head
        if (this.department != null && this.department.getHead() != null) {
            Employee deptHead = this.department.getHead();
            if (!deptHead.getId().equals(this.id) && !containsEmployee(chain, deptHead)) {
                chain.add(deptHead);
            }
        }

        // 3. Parent department heads
        if (this.department != null && this.department.getParent() != null) {
            Department parent = this.department.getParent();
            while (parent != null) {
                if (parent.getHead() != null) {
                    Employee parentHead = parent.getHead();
                    if (!parentHead.getId().equals(this.id) && !containsEmployee(chain, parentHead)) {
                        chain.add(parentHead);
                    }
                }
                parent = parent.getParent();
            }
        }

        return chain;
    }

    /**
     * Helper method to check if an employee is already in the approval chain
     */
    private boolean containsEmployee(List<Employee> chain, Employee employee) {
        return chain.stream().anyMatch(e -> e.getId().equals(employee.getId()));
    }

    // === DATA TAMBAHAN ===
    @Column(name = "kk_number", length = 20)
    private String kkNumber;

    @Column(name = "photo_path", length = 255)
    private String photoPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "spouse_name", length = 100)
    private String spouseName;

    @Column(name = "number_of_dependents")
    private Integer numberOfDependents = 0;

    // === STATUS ===
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "resignation_reason", columnDefinition = "TEXT")
    private String resignationReason;

    // Explicit getters for Lombok workaround
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public EmployeeStatus getStatus() {
        return status;
    }
}
