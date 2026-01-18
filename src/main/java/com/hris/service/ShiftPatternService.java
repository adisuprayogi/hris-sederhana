package com.hris.service;

import com.hris.model.ShiftPackage;
import com.hris.model.ShiftPattern;
import com.hris.repository.ShiftPatternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Shift Pattern Service
 * Pattern (Shift Package + Permissions) - Layer 3 Shift System
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftPatternService {

    private final ShiftPatternRepository shiftPatternRepository;
    private final ShiftPackageService shiftPackageService;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all active shift patterns
     */
    @Transactional(readOnly = true)
    public List<ShiftPattern> getAllShiftPatterns() {
        return shiftPatternRepository.findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    }

    /**
     * Get all shift patterns with shift package fetched
     */
    @Transactional(readOnly = true)
    public List<ShiftPattern> getAllShiftPatternsWithShiftPackage() {
        return shiftPatternRepository.findAllWithShiftPackage();
    }

    /**
     * Get shift pattern by ID
     */
    @Transactional(readOnly = true)
    public ShiftPattern getShiftPatternById(Long id) {
        return shiftPatternRepository.findById(id)
                .filter(sp -> sp.getDeletedAt() == null)
                .orElse(null);
    }

    /**
     * Get shift pattern by ID with shift package
     */
    @Transactional(readOnly = true)
    public ShiftPattern getShiftPatternByIdWithShiftPackage(Long id) {
        return shiftPatternRepository.findByIdWithShiftPackage(id)
                .filter(sp -> sp.getDeletedAt() == null)
                .orElse(null);
    }

    /**
     * Get shift pattern by code
     */
    @Transactional(readOnly = true)
    public ShiftPattern getShiftPatternByCode(String code) {
        return shiftPatternRepository.findByCodeAndDeletedAtIsNull(code).orElse(null);
    }

    /**
     * Create new shift pattern
     */
    @Transactional
    public ShiftPattern createShiftPattern(ShiftPattern shiftPattern) {
        log.info("Creating shift pattern: {}", shiftPattern.getName());

        // Validate code uniqueness
        if (shiftPatternRepository.existsByCodeAndDeletedAtIsNull(shiftPattern.getCode())) {
            throw new IllegalArgumentException("Kode shift pattern sudah digunakan: " + shiftPattern.getCode());
        }

        // Validate shift package reference
        ShiftPackage sp = shiftPackageService.getShiftPackageById(shiftPattern.getShiftPackageId());
        if (sp == null) {
            throw new IllegalArgumentException("Shift package tidak ditemukan");
        }

        // Validate flexible shift settings
        if (shiftPattern.getShiftType() == com.hris.model.enums.ShiftType.FLEXIBLE) {
            if (shiftPattern.getFlexibleStartWindowStart() == null ||
                    shiftPattern.getFlexibleStartWindowEnd() == null ||
                    shiftPattern.getFlexibleRequiredHours() == null) {
                throw new IllegalArgumentException("Flexible shift harus mengisi start window dan required hours");
            }
        }

        ShiftPattern saved = shiftPatternRepository.save(shiftPattern);
        log.info("Shift pattern created successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Update shift pattern
     */
    @Transactional
    public ShiftPattern updateShiftPattern(Long id, ShiftPattern shiftPattern) {
        log.info("Updating shift pattern ID: {}", id);

        ShiftPattern existing = getShiftPatternById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Shift pattern tidak ditemukan dengan ID: " + id);
        }

        // Validate code uniqueness (exclude current)
        if (!existing.getCode().equals(shiftPattern.getCode()) &&
                shiftPatternRepository.existsByCodeAndDeletedAtIsNullAndIdNot(shiftPattern.getCode(), id)) {
            throw new IllegalArgumentException("Kode shift pattern sudah digunakan: " + shiftPattern.getCode());
        }

        // Validate shift package reference
        ShiftPackage sp = shiftPackageService.getShiftPackageById(shiftPattern.getShiftPackageId());
        if (sp == null) {
            throw new IllegalArgumentException("Shift package tidak ditemukan");
        }

        // Validate flexible shift settings
        if (shiftPattern.getShiftType() == com.hris.model.enums.ShiftType.FLEXIBLE) {
            if (shiftPattern.getFlexibleStartWindowStart() == null ||
                    shiftPattern.getFlexibleStartWindowEnd() == null ||
                    shiftPattern.getFlexibleRequiredHours() == null) {
                throw new IllegalArgumentException("Flexible shift harus mengisi start window dan required hours");
            }
        }

        // Update fields
        existing.setName(shiftPattern.getName());
        existing.setCode(shiftPattern.getCode());
        existing.setDescription(shiftPattern.getDescription());
        existing.setShiftPackageId(shiftPattern.getShiftPackageId());
        existing.setShiftType(shiftPattern.getShiftType());
        existing.setFlexibleStartWindowStart(shiftPattern.getFlexibleStartWindowStart());
        existing.setFlexibleStartWindowEnd(shiftPattern.getFlexibleStartWindowEnd());
        existing.setFlexibleRequiredHours(shiftPattern.getFlexibleRequiredHours());
        existing.setIsOvertimeAllowed(shiftPattern.getIsOvertimeAllowed());
        existing.setIsWfhAllowed(shiftPattern.getIsWfhAllowed());
        existing.setIsAttendanceMandatory(shiftPattern.getIsAttendanceMandatory());
        existing.setLateToleranceMinutes(shiftPattern.getLateToleranceMinutes());
        existing.setEarlyLeaveToleranceMinutes(shiftPattern.getEarlyLeaveToleranceMinutes());
        existing.setLateDeductionPerMinute(shiftPattern.getLateDeductionPerMinute());
        existing.setLateDeductionMaxAmount(shiftPattern.getLateDeductionMaxAmount());
        existing.setUnderworkDeductionPerMinute(shiftPattern.getUnderworkDeductionPerMinute());
        existing.setUnderworkDeductionMaxAmount(shiftPattern.getUnderworkDeductionMaxAmount());
        existing.setColor(shiftPattern.getColor());
        existing.setDisplayOrder(shiftPattern.getDisplayOrder());

        // Holiday Override Settings
        existing.setOverrideNationalHoliday(shiftPattern.getOverrideNationalHoliday());
        existing.setOverrideCompanyHoliday(shiftPattern.getOverrideCompanyHoliday());
        existing.setOverrideJointLeave(shiftPattern.getOverrideJointLeave());
        existing.setOverrideWeeklyLeave(shiftPattern.getOverrideWeeklyLeave());

        ShiftPattern saved = shiftPatternRepository.save(existing);
        log.info("Shift pattern updated successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Delete shift pattern (soft delete)
     */
    @Transactional
    public void deleteShiftPattern(Long id) {
        log.info("Deleting shift pattern ID: {}", id);

        ShiftPattern existing = getShiftPatternById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Shift pattern tidak ditemukan dengan ID: " + id);
        }

        // Check if used in employee shift settings
        // (Will implement this check after creating EmployeeShiftService)

        existing.softDelete(null);
        shiftPatternRepository.save(existing);

        log.info("Shift pattern deleted successfully: {}", id);
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    /**
     * Get default shift pattern (for new employees)
     */
    @Transactional(readOnly = true)
    public ShiftPattern getDefaultShiftPattern() {
        // Return ST001 as default
        return getShiftPatternByCode("ST001");
    }

    /**
     * Calculate late deduction for a shift pattern
     */
    public BigDecimal calculateLateDeduction(ShiftPattern pattern, int lateMinutes) {
        if (pattern == null) {
            return BigDecimal.ZERO;
        }
        return pattern.calculateLateDeduction(lateMinutes);
    }

    /**
     * Calculate underwork deduction for a shift pattern
     */
    public BigDecimal calculateUnderworkDeduction(ShiftPattern pattern, int underworkMinutes) {
        if (pattern == null) {
            return BigDecimal.ZERO;
        }
        return pattern.calculateUnderworkDeduction(underworkMinutes);
    }
}
