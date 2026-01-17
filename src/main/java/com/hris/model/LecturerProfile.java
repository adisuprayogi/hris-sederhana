package com.hris.model;

import com.hris.model.enums.LecturerEmploymentStatus;
import com.hris.model.enums.LecturerRank;
import com.hris.model.enums.LecturerWorkStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Lecturer Profile Entity
 * Extends Employee data with lecturer-specific information
 */
@Entity
@Table(name = "lecturer_profiles")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LecturerProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // EMPLOYEE RELATIONSHIP
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    // =====================================================
    // LECTURER IDENTIFICATION
    // =====================================================

    @Column(name = "nidn", unique = true, length = 20)
    private String nidn;

    @Column(name = "last_education", length = 100)
    private String lastEducation;

    @Column(name = "expertise", length = 255)
    private String expertise;

    // =====================================================
    // LECTURER STATUS
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "lecturer_rank", nullable = false, length = 20)
    private LecturerRank lecturerRank;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private LecturerEmploymentStatus employmentStatus = LecturerEmploymentStatus.DOSEN_TETAP;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status", nullable = false, length = 10)
    private LecturerWorkStatus workStatus = LecturerWorkStatus.ACTIVE;

    // =====================================================
    // HOMEBASE PRODI
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homebase_prodi_id")
    private Department homebaseProdi;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if lecturer is active (not on leave or retired)
     */
    public boolean isActive() {
        return workStatus == LecturerWorkStatus.ACTIVE;
    }

    /**
     * Check if lecturer is permanent (dosen tetap)
     */
    public boolean isPermanent() {
        return employmentStatus == LecturerEmploymentStatus.DOSEN_TETAP;
    }

    /**
     * Get rank display name
     */
    public String getRankDisplayName() {
        return lecturerRank != null ? lecturerRank.getDisplayName() : "-";
    }

    /**
     * Get employment status display name
     */
    public String getEmploymentStatusDisplayName() {
        return employmentStatus != null ? employmentStatus.getDisplayName() : "-";
    }

    /**
     * Get work status display name
     */
    public String getWorkStatusDisplayName() {
        return workStatus != null ? workStatus.getDisplayName() : "-";
    }

    /**
     * Get full name from employee
     */
    public String getFullName() {
        return employee != null ? employee.getFullName() : "-";
    }

    /**
     * Get NIK from employee
     */
    public String getNik() {
        return employee != null ? employee.getNik() : "-";
    }

    /**
     * Get department from employee
     */
    public String getDepartmentName() {
        return employee != null && employee.getDepartment() != null
            ? employee.getDepartment().getName()
            : "-";
    }

    /**
     * Get position from employee
     */
    public String getPositionName() {
        return employee != null && employee.getPosition() != null
            ? employee.getPosition().getName()
            : "-";
    }
}
