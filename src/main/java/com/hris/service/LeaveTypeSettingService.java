package com.hris.service;

import com.hris.model.LeaveTypeSetting;
import com.hris.model.enums.GenderRestriction;
import com.hris.model.enums.LeaveTypeEnum;
import com.hris.repository.LeaveTypeSettingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk LeaveTypeSetting Entity
 * Menangani operasi CRUD dan logika bisnis terkait pengaturan jenis cuti
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveTypeSettingService {

    private final LeaveTypeSettingRepository repository;

    /**
     * Get all leave types ordered by year and code
     */
    public List<LeaveTypeSetting> getAllLeaveTypes() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "year").and(Sort.by("code")));
    }

    /**
     * Get active leave types by year
     */
    public List<LeaveTypeSetting> getActiveLeaveTypesByYear(Integer year) {
        return repository.findByYearAndIsActiveTrueAndDeletedAtIsNullOrderByCode(year);
    }

    /**
     * Get all leave types by year (including inactive)
     */
    public List<LeaveTypeSetting> getAllLeaveTypesByYear(Integer year) {
        return repository.findByYearAndDeletedAtIsNullOrderByCode(year);
    }

    /**
     * Get quota-based leave types by year
     */
    public List<LeaveTypeSetting> getQuotaTypesByYear(Integer year) {
        return repository.findQuotaTypesByYear(year);
    }

    /**
     * Get leave type by ID
     */
    public Optional<LeaveTypeSetting> getLeaveTypeById(Long id) {
        return repository.findById(id);
    }

    /**
     * Get leave type by code and year
     */
    public Optional<LeaveTypeSetting> getLeaveTypeByCodeAndYear(String code, Integer year) {
        return repository.findByCodeAndYearAndDeletedAtIsNull(code, year);
    }

    /**
     * Create new leave type setting
     */
    @Transactional
    public LeaveTypeSetting createLeaveType(LeaveTypeSetting leaveType) {
        // Validate: check if code already exists for this year
        if (repository.existsByCodeAndYearAndDeletedAtIsNull(leaveType.getCode(), leaveType.getYear())) {
            throw new IllegalArgumentException(
                    "Leave type with code '" + leaveType.getCode() + "' already exists for year " + leaveType.getYear()
            );
        }

        // Validate carry forward settings
        if (leaveType.getAllowCarryForward()) {
            if (leaveType.getMaxCarryForwardDays() == null ||
                leaveType.getCarryForwardExpiryMonth() == null ||
                leaveType.getCarryForwardExpiryDay() == null) {
                throw new IllegalArgumentException(
                        "Carry forward settings must include max days, expiry month, and expiry day"
                );
            }
        }

        // Validate quota settings
        if (leaveType.getLeaveType() == LeaveTypeEnum.QUOTA && leaveType.getAnnualQuota() == null) {
            throw new IllegalArgumentException(
                    "Annual quota is required for QUOTA leave types"
            );
        }

        LeaveTypeSetting saved = repository.save(leaveType);
        log.info("Created new leave type: {} ({}) for year {}", saved.getName(), saved.getCode(), saved.getYear());
        return saved;
    }

    /**
     * Update leave type setting
     */
    @Transactional
    public LeaveTypeSetting updateLeaveType(Long id, LeaveTypeSetting details) {
        LeaveTypeSetting leaveType = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found with id: " + id));

        // Validate: check if code conflict with other leave type
        Optional<LeaveTypeSetting> existingByCode = repository.findByCodeAndYearAndDeletedAtIsNull(
                details.getCode(), details.getYear()
        );
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "Another leave type with code '" + details.getCode() + "' already exists for year " + details.getYear()
            );
        }

        // Validate carry forward settings
        if (details.getAllowCarryForward()) {
            if (details.getMaxCarryForwardDays() == null ||
                details.getCarryForwardExpiryMonth() == null ||
                details.getCarryForwardExpiryDay() == null) {
                throw new IllegalArgumentException(
                        "Carry forward settings must include max days, expiry month, and expiry day"
                );
            }
        }

        // Update fields
        leaveType.setYear(details.getYear());
        leaveType.setCode(details.getCode());
        leaveType.setName(details.getName());
        leaveType.setLeaveType(details.getLeaveType());
        leaveType.setAnnualQuota(details.getAnnualQuota());
        leaveType.setAllowCarryForward(details.getAllowCarryForward());
        leaveType.setMaxCarryForwardDays(details.getMaxCarryForwardDays());
        leaveType.setCarryForwardExpiryMonth(details.getCarryForwardExpiryMonth());
        leaveType.setCarryForwardExpiryDay(details.getCarryForwardExpiryDay());
        leaveType.setMinYearsOfService(details.getMinYearsOfService());
        leaveType.setGenderRestriction(details.getGenderRestriction());
        leaveType.setIsPaid(details.getIsPaid());
        leaveType.setRequireProof(details.getRequireProof());
        leaveType.setProofDescription(details.getProofDescription());
        leaveType.setDescription(details.getDescription());
        leaveType.setIsActive(details.getIsActive());

        LeaveTypeSetting updated = repository.save(leaveType);
        log.info("Updated leave type: {} ({}) for year {}", updated.getName(), updated.getCode(), updated.getYear());
        return updated;
    }

    /**
     * Delete leave type (soft delete)
     */
    @Transactional
    public void deleteLeaveType(Long id) {
        LeaveTypeSetting leaveType = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found with id: " + id));

        leaveType.setDeletedAt(java.time.LocalDateTime.now());
        repository.save(leaveType);
        log.info("Deleted leave type: {} ({}) for year {}", leaveType.getName(), leaveType.getCode(), leaveType.getYear());
    }

    /**
     * Toggle active status
     */
    @Transactional
    public LeaveTypeSetting toggleStatus(Long id) {
        LeaveTypeSetting leaveType = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found with id: " + id));

        leaveType.setIsActive(!leaveType.getIsActive());
        LeaveTypeSetting updated = repository.save(leaveType);
        log.info("Toggled leave type status: {} ({}) - now {}", updated.getName(), updated.getCode(),
                updated.getIsActive() ? "ACTIVE" : "INACTIVE");
        return updated;
    }

    /**
     * Get statistics
     */
    public LeaveTypeStats getStats(Integer year) {
        List<LeaveTypeSetting> allTypes = getAllLeaveTypesByYear(year);
        List<LeaveTypeSetting> activeTypes = getActiveLeaveTypesByYear(year);

        long quotaCount = activeTypes.stream()
                .filter(lt -> lt.getLeaveType() == LeaveTypeEnum.QUOTA)
                .count();

        long noQuotaCount = activeTypes.stream()
                .filter(lt -> lt.getLeaveType() == LeaveTypeEnum.NO_QUOTA)
                .count();

        long withCarryForward = activeTypes.stream()
                .filter(LeaveTypeSetting::hasCarryForward)
                .count();

        long paidCount = activeTypes.stream()
                .filter(lt -> Boolean.TRUE.equals(lt.getIsPaid()))
                .count();

        return new LeaveTypeStats(
                allTypes.size(),
                activeTypes.size(),
                quotaCount,
                noQuotaCount,
                withCarryForward,
                paidCount
        );
    }

    /**
     * Get all available years
     */
    public List<Integer> getAllYears() {
        return repository.findAllDistinctYears();
    }

    /**
     * Check if employee is eligible for leave type
     */
    public boolean isEmployeeEligible(Long leaveTypeId, Integer yearsOfService, GenderRestriction gender) {
        LeaveTypeSetting leaveType = repository.findById(leaveTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found"));

        if (!leaveType.getIsActive()) {
            return false;
        }

        // Check years of service
        if (yearsOfService != null && yearsOfService < leaveType.getMinYearsOfService()) {
            return false;
        }

        // Check gender restriction
        if (leaveType.getGenderRestriction() != GenderRestriction.ALL &&
                leaveType.getGenderRestriction() != gender) {
            return false;
        }

        return true;
    }

    /**
     * DTO for statistics
     */
    public record LeaveTypeStats(
            long totalTypes,
            long activeTypes,
            long quotaTypes,
            long noQuotaTypes,
            long withCarryForward,
            long paidTypes
    ) {}
}
