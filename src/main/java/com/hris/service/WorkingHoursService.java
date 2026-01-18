package com.hris.service;

import com.hris.model.WorkingHours;
import com.hris.repository.WorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Working Hours Service
 * Master Jam Kerja - Layer 1 Shift System
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all active working hours
     */
    @Transactional(readOnly = true)
    public List<WorkingHours> getAllWorkingHours() {
        return workingHoursRepository.findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    }

    /**
     * Get working hours by ID
     */
    @Transactional(readOnly = true)
    public WorkingHours getWorkingHoursById(Long id) {
        return workingHoursRepository.findById(id)
                .filter(wh -> wh.getDeletedAt() == null)
                .orElse(null);
    }

    /**
     * Get working hours by code
     */
    @Transactional(readOnly = true)
    public WorkingHours getWorkingHoursByCode(String code) {
        return workingHoursRepository.findByCodeAndDeletedAtIsNull(code).orElse(null);
    }

    /**
     * Create new working hours
     */
    @Transactional
    public WorkingHours createWorkingHours(WorkingHours workingHours) {
        log.info("Creating working hours: {}", workingHours.getName());

        // Validate code uniqueness
        if (workingHoursRepository.existsByCodeAndDeletedAtIsNull(workingHours.getCode())) {
            throw new IllegalArgumentException("Kode working hours sudah digunakan: " + workingHours.getCode());
        }

        WorkingHours saved = workingHoursRepository.save(workingHours);
        log.info("Working hours created successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Update working hours
     */
    @Transactional
    public WorkingHours updateWorkingHours(Long id, WorkingHours workingHours) {
        log.info("Updating working hours ID: {}", id);

        WorkingHours existing = getWorkingHoursById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Working hours tidak ditemukan dengan ID: " + id);
        }

        // Validate code uniqueness (exclude current)
        if (!existing.getCode().equals(workingHours.getCode()) &&
                workingHoursRepository.existsByCodeAndDeletedAtIsNullAndIdNot(workingHours.getCode(), id)) {
            throw new IllegalArgumentException("Kode working hours sudah digunakan: " + workingHours.getCode());
        }

        // Update fields
        existing.setName(workingHours.getName());
        existing.setCode(workingHours.getCode());
        existing.setDescription(workingHours.getDescription());
        existing.setStartTime(workingHours.getStartTime());
        existing.setEndTime(workingHours.getEndTime());
        existing.setIsOvernight(workingHours.getIsOvernight());
        existing.setBreakDurationMinutes(workingHours.getBreakDurationMinutes());
        existing.setRequiredWorkHours(workingHours.getRequiredWorkHours());
        existing.setDisplayOrder(workingHours.getDisplayOrder());
        existing.setColor(workingHours.getColor());

        WorkingHours saved = workingHoursRepository.save(existing);
        log.info("Working hours updated successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Delete working hours (soft delete)
     */
    @Transactional
    public void deleteWorkingHours(Long id) {
        log.info("Deleting working hours ID: {}", id);

        WorkingHours existing = getWorkingHoursById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Working hours tidak ditemukan dengan ID: " + id);
        }

        // Check if used in shift packages
        // (Will implement this check after creating ShiftPackage)

        existing.softDelete(null);
        workingHoursRepository.save(existing);

        log.info("Working hours deleted successfully: {}", id);
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    /**
     * Get OFF working hours
     */
    @Transactional(readOnly = true)
    public WorkingHours getOffWorkingHours() {
        return workingHoursRepository.findOffWorkingHours().orElse(null);
    }

    /**
     * Check if working hours is OFF
     */
    public boolean isOffWorkingHours(Long workingHoursId) {
        if (workingHoursId == null) {
            return true;
        }
        WorkingHours wh = getWorkingHoursById(workingHoursId);
        return wh != null && wh.isOff();
    }
}
