package com.hris.service;

import com.hris.model.ShiftPackage;
import com.hris.model.WorkingHours;
import com.hris.repository.ShiftPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Shift Package Service
 * Paket Shift (Kombinasi Working Hours per Hari) - Layer 2 Shift System
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftPackageService {

    private final ShiftPackageRepository shiftPackageRepository;
    private final WorkingHoursService workingHoursService;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all active shift packages
     */
    @Transactional(readOnly = true)
    public List<ShiftPackage> getAllShiftPackages() {
        return shiftPackageRepository.findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    }

    /**
     * Get all shift packages with working hours fetched
     */
    @Transactional(readOnly = true)
    public List<ShiftPackage> getAllShiftPackagesWithWorkingHours() {
        return shiftPackageRepository.findAllWithWorkingHours();
    }

    /**
     * Get shift package by ID
     */
    @Transactional(readOnly = true)
    public ShiftPackage getShiftPackageById(Long id) {
        return shiftPackageRepository.findById(id)
                .filter(sp -> sp.getDeletedAt() == null)
                .orElse(null);
    }

    /**
     * Get shift package by ID with working hours
     */
    @Transactional(readOnly = true)
    public ShiftPackage getShiftPackageByIdWithWorkingHours(Long id) {
        return shiftPackageRepository.findByIdWithWorkingHours(id)
                .filter(sp -> sp.getDeletedAt() == null)
                .orElse(null);
    }

    /**
     * Get shift package by code
     */
    @Transactional(readOnly = true)
    public ShiftPackage getShiftPackageByCode(String code) {
        return shiftPackageRepository.findByCodeAndDeletedAtIsNull(code).orElse(null);
    }

    /**
     * Create new shift package
     */
    @Transactional
    public ShiftPackage createShiftPackage(ShiftPackage shiftPackage) {
        log.info("Creating shift package: {}", shiftPackage.getName());

        // Validate code uniqueness
        if (shiftPackageRepository.existsByCodeAndDeletedAtIsNull(shiftPackage.getCode())) {
            throw new IllegalArgumentException("Kode shift package sudah digunakan: " + shiftPackage.getCode());
        }

        // Validate working hours references
        validateWorkingHours(shiftPackage);

        ShiftPackage saved = shiftPackageRepository.save(shiftPackage);
        log.info("Shift package created successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Update shift package
     */
    @Transactional
    public ShiftPackage updateShiftPackage(Long id, ShiftPackage shiftPackage) {
        log.info("Updating shift package ID: {}", id);

        ShiftPackage existing = getShiftPackageById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Shift package tidak ditemukan dengan ID: " + id);
        }

        // Validate code uniqueness (exclude current)
        if (!existing.getCode().equals(shiftPackage.getCode()) &&
                shiftPackageRepository.existsByCodeAndDeletedAtIsNullAndIdNot(shiftPackage.getCode(), id)) {
            throw new IllegalArgumentException("Kode shift package sudah digunakan: " + shiftPackage.getCode());
        }

        // Validate working hours references
        validateWorkingHours(shiftPackage);

        // Update fields
        existing.setName(shiftPackage.getName());
        existing.setCode(shiftPackage.getCode());
        existing.setDescription(shiftPackage.getDescription());
        existing.setSundayWorkingHoursId(shiftPackage.getSundayWorkingHoursId());
        existing.setMondayWorkingHoursId(shiftPackage.getMondayWorkingHoursId());
        existing.setTuesdayWorkingHoursId(shiftPackage.getTuesdayWorkingHoursId());
        existing.setWednesdayWorkingHoursId(shiftPackage.getWednesdayWorkingHoursId());
        existing.setThursdayWorkingHoursId(shiftPackage.getThursdayWorkingHoursId());
        existing.setFridayWorkingHoursId(shiftPackage.getFridayWorkingHoursId());
        existing.setSaturdayWorkingHoursId(shiftPackage.getSaturdayWorkingHoursId());
        existing.setDisplayOrder(shiftPackage.getDisplayOrder());
        existing.setColor(shiftPackage.getColor());

        ShiftPackage saved = shiftPackageRepository.save(existing);
        log.info("Shift package updated successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Delete shift package (soft delete)
     */
    @Transactional
    public void deleteShiftPackage(Long id) {
        log.info("Deleting shift package ID: {}", id);

        ShiftPackage existing = getShiftPackageById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Shift package tidak ditemukan dengan ID: " + id);
        }

        // Check if used in shift patterns
        // (Will implement this check after creating ShiftPattern)

        existing.softDelete(null);
        shiftPackageRepository.save(existing);

        log.info("Shift package deleted successfully: {}", id);
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    /**
     * Get working hours for a specific day
     */
    @Transactional(readOnly = true)
    public WorkingHours getWorkingHoursForDay(Long shiftPackageId, DayOfWeek day) {
        ShiftPackage sp = getShiftPackageById(shiftPackageId);
        if (sp == null) {
            return null;
        }

        Long workingHoursId = sp.getWorkingHoursIdByDay(day);
        if (workingHoursId == null) {
            return workingHoursService.getOffWorkingHours();
        }

        return workingHoursService.getWorkingHoursById(workingHoursId);
    }

    /**
     * Validate working hours references
     */
    private void validateWorkingHours(ShiftPackage shiftPackage) {
        // Validate Monday-Friday (required)
        validateWorkingHoursExists(shiftPackage.getMondayWorkingHoursId(), "Monday");
        validateWorkingHoursExists(shiftPackage.getTuesdayWorkingHoursId(), "Tuesday");
        validateWorkingHoursExists(shiftPackage.getWednesdayWorkingHoursId(), "Wednesday");
        validateWorkingHoursExists(shiftPackage.getThursdayWorkingHoursId(), "Thursday");
        validateWorkingHoursExists(shiftPackage.getFridayWorkingHoursId(), "Friday");

        // Validate Saturday-Sunday (optional)
        if (shiftPackage.getSaturdayWorkingHoursId() != null) {
            validateWorkingHoursExists(shiftPackage.getSaturdayWorkingHoursId(), "Saturday");
        }
        if (shiftPackage.getSundayWorkingHoursId() != null) {
            validateWorkingHoursExists(shiftPackage.getSundayWorkingHoursId(), "Sunday");
        }
    }

    private void validateWorkingHoursExists(Long workingHoursId, String dayName) {
        if (workingHoursId != null) {
            WorkingHours wh = workingHoursService.getWorkingHoursById(workingHoursId);
            if (wh == null) {
                throw new IllegalArgumentException("Working hours untuk " + dayName + " tidak ditemukan");
            }
        }
    }
}
