package com.hris.service;

import com.hris.model.Department;
import com.hris.model.Employee;
import com.hris.model.LecturerProfile;
import com.hris.model.enums.LecturerEmploymentStatus;
import com.hris.model.enums.LecturerRank;
import com.hris.model.enums.LecturerWorkStatus;
import com.hris.repository.DepartmentRepository;
import com.hris.repository.EmployeeRepository;
import com.hris.repository.LecturerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lecturer Service
 * Handles business logic for lecturer management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LecturerService {

    private final LecturerProfileRepository lecturerProfileRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all active lecturers
     */
    public List<LecturerProfile> getAllLecturers() {
        return lecturerProfileRepository.findAllActive();
    }

    /**
     * Get lecturer profile by ID
     */
    @Transactional(readOnly = true)
    public LecturerProfile getLecturerById(Long id) {
        return lecturerProfileRepository.findByIdWithEmployee(id).orElse(null);
    }

    /**
     * Get lecturer profile by employee ID
     */
    @Transactional(readOnly = true)
    public LecturerProfile getLecturerByEmployeeId(Long employeeId) {
        return lecturerProfileRepository.findByEmployeeIdAndDeletedAtIsNull(employeeId).orElse(null);
    }

    /**
     * Get lecturer profile by NIDN
     */
    @Transactional(readOnly = true)
    public LecturerProfile getLecturerByNidn(String nidn) {
        return lecturerProfileRepository.findByNidnAndDeletedAtIsNull(nidn).orElse(null);
    }

    /**
     * Create new lecturer profile
     */
    @Transactional
    public LecturerProfile createLecturerProfile(LecturerProfile lecturerProfile) {
        log.info("Creating lecturer profile for employee ID: {}", lecturerProfile.getEmployee().getId());

        // Validate: employee must exist
        Employee employee = employeeRepository.findById(lecturerProfile.getEmployee().getId()).orElse(null);
        if (employee == null) {
            throw new IllegalArgumentException("Employee tidak ditemukan");
        }

        // Validate: employee must not have lecturer profile yet
        if (lecturerProfileRepository.findByEmployeeIdAndDeletedAtIsNull(employee.getId()).isPresent()) {
            throw new IllegalArgumentException("Employee sudah memiliki profil dosen");
        }

        // Validate: NIDN uniqueness
        if (lecturerProfile.getNidn() != null &&
            lecturerProfileRepository.existsByNidnAndDeletedAtIsNull(lecturerProfile.getNidn())) {
            throw new IllegalArgumentException("NIDN sudah terdaftar: " + lecturerProfile.getNidn());
        }

        // Validate: homebase prodi must be a prodi (is_prodi = true)
        if (lecturerProfile.getHomebaseProdi() != null) {
            Department homebase = departmentRepository.findById(lecturerProfile.getHomebaseProdi().getId()).orElse(null);
            if (homebase == null) {
                throw new IllegalArgumentException("Homebase prodi tidak ditemukan");
            }
            if (homebase.getIsProdi() == null || !homebase.getIsProdi()) {
                throw new IllegalArgumentException("Homebase harus berupa program studi (is_prodi = true)");
            }
            lecturerProfile.setHomebaseProdi(homebase);
        }

        LecturerProfile saved = lecturerProfileRepository.save(lecturerProfile);
        log.info("Lecturer profile created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update existing lecturer profile
     */
    @Transactional
    public LecturerProfile updateLecturerProfile(Long id, LecturerProfile lecturerProfile) {
        log.info("Updating lecturer profile ID: {}", id);

        LecturerProfile existing = getLecturerById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Profil dosen tidak ditemukan dengan ID: " + id);
        }

        // Validate: NIDN uniqueness (exclude current)
        if (lecturerProfile.getNidn() != null &&
            !lecturerProfile.getNidn().equals(existing.getNidn()) &&
            lecturerProfileRepository.existsByNidnAndDeletedAtIsNullAndIdNot(lecturerProfile.getNidn(), id)) {
            throw new IllegalArgumentException("NIDN sudah terdaftar: " + lecturerProfile.getNidn());
        }

        // Validate: homebase prodi must be a prodi
        if (lecturerProfile.getHomebaseProdi() != null) {
            Department homebase = departmentRepository.findById(lecturerProfile.getHomebaseProdi().getId()).orElse(null);
            if (homebase == null) {
                throw new IllegalArgumentException("Homebase prodi tidak ditemukan");
            }
            if (homebase.getIsProdi() == null || !homebase.getIsProdi()) {
                throw new IllegalArgumentException("Homebase harus berupa program studi (is_prodi = true)");
            }
        }

        // Update fields
        existing.setNidn(lecturerProfile.getNidn());
        existing.setLastEducation(lecturerProfile.getLastEducation());
        existing.setExpertise(lecturerProfile.getExpertise());
        existing.setLecturerRank(lecturerProfile.getLecturerRank());
        existing.setEmploymentStatus(lecturerProfile.getEmploymentStatus());
        existing.setWorkStatus(lecturerProfile.getWorkStatus());
        existing.setHomebaseProdi(lecturerProfile.getHomebaseProdi());

        LecturerProfile saved = lecturerProfileRepository.save(existing);
        log.info("Lecturer profile updated successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Soft delete lecturer profile
     * Also sets employee_id to NULL to free up the unique constraint
     */
    @Transactional
    public void deleteLecturerProfile(Long id) {
        log.info("Deleting lecturer profile ID: {}", id);

        LecturerProfile lecturerProfile = getLecturerById(id);
        if (lecturerProfile == null) {
            throw new IllegalArgumentException("Profil dosen tidak ditemukan dengan ID: " + id);
        }

        // Store employee_id for logging
        Long employeeId = lecturerProfile.getEmployee() != null ? lecturerProfile.getEmployee().getId() : null;

        // Soft delete and set employee to NULL to free up the unique constraint
        lecturerProfile.softDelete(null);
        lecturerProfile.setEmployee(null);
        lecturerProfileRepository.save(lecturerProfile);

        log.info("Lecturer profile deleted successfully: {} (was linked to employee: {})", id, employeeId);
    }

    // =====================================================
    // QUERY METHODS
    // =====================================================

    /**
     * Search lecturers with filters
     */
    public List<LecturerProfile> searchLecturers(String search, LecturerRank rank,
                                                   LecturerEmploymentStatus empStatus,
                                                   LecturerWorkStatus workStatus,
                                                   Long prodiId) {
        log.info("Searching lecturers - search: {}, rank: {}, empStatus: {}, workStatus: {}, prodiId: {}",
            search, rank, empStatus, workStatus, prodiId);
        return lecturerProfileRepository.searchLecturers(search, rank, empStatus, workStatus, prodiId);
    }

    /**
     * Get lecturers by homebase prodi
     */
    public List<LecturerProfile> getLecturersByHomebaseProdi(Long prodiId) {
        return lecturerProfileRepository.findByHomebaseProdiIdAndDeletedAtIsNull(prodiId);
    }

    /**
     * Get lecturers by employment status
     */
    public List<LecturerProfile> getLecturersByEmploymentStatus(LecturerEmploymentStatus status) {
        return lecturerProfileRepository.findByEmploymentStatusAndDeletedAtIsNull(status);
    }

    /**
     * Get lecturers by work status
     */
    public List<LecturerProfile> getLecturersByWorkStatus(LecturerWorkStatus status) {
        return lecturerProfileRepository.findByWorkStatusAndDeletedAtIsNull(status);
    }

    /**
     * Get lecturers by rank
     */
    public List<LecturerProfile> getLecturersByRank(LecturerRank rank) {
        return lecturerProfileRepository.findByLecturerRankAndDeletedAtIsNull(rank);
    }

    /**
     * Count lecturers by homebase prodi
     */
    public long countByHomebaseProdi(Long prodiId) {
        return lecturerProfileRepository.countByHomebaseProdiIdAndDeletedAtIsNull(prodiId);
    }

    /**
     * Count lecturers by employment status
     */
    public long countByEmploymentStatus(LecturerEmploymentStatus status) {
        return lecturerProfileRepository.countByEmploymentStatusAndDeletedAtIsNull(status);
    }

    /**
     * Count lecturers by work status
     */
    public long countByWorkStatus(LecturerWorkStatus status) {
        return lecturerProfileRepository.countByWorkStatusAndDeletedAtIsNull(status);
    }

    /**
     * Count total active lecturers
     */
    public long countActiveLecturers() {
        return lecturerProfileRepository.countByDeletedAtIsNull();
    }
}
