package com.hris.service;

import com.hris.model.*;
import com.hris.repository.EmployeeShiftScheduleRepository;
import com.hris.repository.EmployeeShiftSettingRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Employee Shift Service
 * Handle assignment dan retrieval shift untuk employee
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeShiftService {

    private final EmployeeShiftSettingRepository employeeShiftSettingRepository;
    private final EmployeeShiftScheduleRepository employeeShiftScheduleRepository;
    private final ShiftPatternService shiftPatternService;
    private final ShiftPackageService shiftPackageService;
    private final WorkingHoursService workingHoursService;

    // =====================================================
    // SHIFT ASSIGNMENT
    // =====================================================

    /**
     * Assign shift pattern to employee with effective date
     * Auto-close previous assignment
     */
    @Transactional
    public EmployeeShiftSetting assignShiftPattern(Long employeeId, Long shiftPatternId,
                                                    LocalDate effectiveFrom, String reason, String notes, Long createdBy) {
        log.info("Assigning shift pattern {} to employee {} from {}", shiftPatternId, employeeId, effectiveFrom);

        // Validate shift pattern
        ShiftPattern pattern = shiftPatternService.getShiftPatternById(shiftPatternId);
        if (pattern == null) {
            throw new IllegalArgumentException("Shift pattern tidak ditemukan");
        }

        // Find and close previous assignment if exists
        LocalDate dayBefore = effectiveFrom.minusDays(1);
        employeeShiftSettingRepository.findOpenSettingBeforeDate(employeeId, dayBefore)
                .ifPresent(prev -> {
                    prev.setEffectiveTo(dayBefore);
                    employeeShiftSettingRepository.save(prev);
                    log.info("Closed previous assignment ID: {}", prev.getId());
                });

        // Create new assignment
        EmployeeShiftSetting setting = EmployeeShiftSetting.builder()
                .employeeId(employeeId)
                .shiftPatternId(shiftPatternId)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(null)
                .reason(reason)
                .notes(notes)
                .createdBy(createdBy)
                .build();

        EmployeeShiftSetting saved = employeeShiftSettingRepository.save(setting);
        log.info("Shift pattern assigned successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Get current active shift pattern for employee
     */
    @Transactional(readOnly = true)
    public ShiftPattern getActiveShiftPattern(Long employeeId) {
        return getActiveShiftPattern(employeeId, LocalDate.now());
    }

    /**
     * Get active shift pattern for employee on specific date
     */
    @Transactional(readOnly = true)
    public ShiftPattern getActiveShiftPattern(Long employeeId, LocalDate date) {
        return employeeShiftSettingRepository.findActiveByEmployeeAndDate(employeeId, date)
                .map(EmployeeShiftSetting::getShiftPatternId)
                .map(shiftPatternService::getShiftPatternById)
                .orElse(null);
    }

    /**
     * Get all shift settings for employee
     */
    @Transactional(readOnly = true)
    public List<EmployeeShiftSetting> getEmployeeShiftSettings(Long employeeId) {
        return employeeShiftSettingRepository.findByEmployeeIdAndDeletedAtIsNullOrderByEffectiveFromDesc(employeeId);
    }

    // =====================================================
    // SHIFT OVERRIDE (SCHEDULE)
    // =====================================================

    /**
     * Create override schedule for employee on specific date
     */
    @Transactional
    public EmployeeShiftSchedule createOverrideSchedule(Long employeeId, LocalDate scheduleDate,
                                                         Long workingHoursId, Boolean overrideIsWfh,
                                                         Boolean overrideIsOvertimeAllowed,
                                                         Boolean overrideAttendanceMandatory,
                                                         String notes, Long createdBy) {
        log.info("Creating override schedule for employee {} on {}", employeeId, scheduleDate);

        // Validate working hours
        if (workingHoursId != null) {
            WorkingHours wh = workingHoursService.getWorkingHoursById(workingHoursId);
            if (wh == null) {
                throw new IllegalArgumentException("Working hours tidak ditemukan");
            }
        }

        // Check if schedule already exists
        EmployeeShiftSchedule existing = employeeShiftScheduleRepository
                .findByEmployeeIdAndScheduleDateAndDeletedAtIsNull(employeeId, scheduleDate)
                .orElse(null);

        if (existing != null) {
            // Update existing
            existing.setWorkingHoursId(workingHoursId);
            existing.setOverrideIsWfh(overrideIsWfh);
            existing.setOverrideIsOvertimeAllowed(overrideIsOvertimeAllowed);
            existing.setOverrideAttendanceMandatory(overrideAttendanceMandatory);
            existing.setNotes(notes);
            return employeeShiftScheduleRepository.save(existing);
        }

        // Create new
        EmployeeShiftSchedule schedule = EmployeeShiftSchedule.builder()
                .employeeId(employeeId)
                .scheduleDate(scheduleDate)
                .workingHoursId(workingHoursId)
                .overrideIsWfh(overrideIsWfh)
                .overrideIsOvertimeAllowed(overrideIsOvertimeAllowed)
                .overrideAttendanceMandatory(overrideAttendanceMandatory)
                .notes(notes)
                .createdBy(createdBy)
                .build();

        EmployeeShiftSchedule saved = employeeShiftScheduleRepository.save(schedule);
        log.info("Override schedule created successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Delete override schedule (soft delete)
     */
    @Transactional
    public void deleteOverrideSchedule(Long scheduleId) {
        log.info("Deleting override schedule ID: {}", scheduleId);

        EmployeeShiftSchedule schedule = employeeShiftScheduleRepository.findById(scheduleId).orElse(null);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule tidak ditemukan");
        }

        schedule.softDelete(null);
        employeeShiftScheduleRepository.save(schedule);

        log.info("Override schedule deleted successfully: {}", scheduleId);
    }

    /**
     * Get override schedule for employee on specific date
     */
    @Transactional(readOnly = true)
    public EmployeeShiftSchedule getOverrideSchedule(Long employeeId, LocalDate date) {
        return employeeShiftScheduleRepository
                .findByEmployeeIdAndScheduleDateAndDeletedAtIsNull(employeeId, date)
                .orElse(null);
    }

    /**
     * Get all override schedules for employee in date range
     */
    @Transactional(readOnly = true)
    public List<EmployeeShiftSchedule> getOverrideSchedules(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return employeeShiftScheduleRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
    }

    // =====================================================
    // SHIFT ASSIGNMENT RESULT (DTO)
    // =====================================================

    /**
     * Get shift assignment for employee on specific date
     * This is the main method that combines pattern and override
     */
    @Transactional(readOnly = true)
    public ShiftAssignmentResult getShiftAssignment(Long employeeId, LocalDate date) {
        // 1. Check override first
        EmployeeShiftSchedule override = getOverrideSchedule(employeeId, date);
        if (override != null) {
            return buildFromOverride(override, date);
        }

        // 2. Get from pattern
        ShiftPattern pattern = getActiveShiftPattern(employeeId, date);
        if (pattern == null) {
            return ShiftAssignmentResult.builder()
                    .employeeId(employeeId)
                    .date(date)
                    .isWorkingDay(false)
                    .isOffDay(true)
                    .build();
        }

        return buildFromPattern(pattern, date);
    }

    private ShiftAssignmentResult buildFromOverride(EmployeeShiftSchedule override, LocalDate date) {
        WorkingHours wh = null;
        if (override.getWorkingHoursId() != null) {
            wh = workingHoursService.getWorkingHoursById(override.getWorkingHoursId());
        }

        Boolean isWfh = override.getOverrideIsWfh();
        Boolean isOvertimeAllowed = override.getOverrideIsOvertimeAllowed();
        Boolean isAttendanceMandatory = override.getOverrideAttendanceMandatory();

        // If override values are null, get from pattern
        if (isWfh == null || isOvertimeAllowed == null || isAttendanceMandatory == null) {
            ShiftPattern pattern = getActiveShiftPattern(override.getEmployeeId(), date);
            if (pattern != null) {
                if (isWfh == null) isWfh = pattern.getIsWfhAllowed();
                if (isOvertimeAllowed == null) isOvertimeAllowed = pattern.getIsOvertimeAllowed();
                if (isAttendanceMandatory == null) isAttendanceMandatory = pattern.getIsAttendanceMandatory();
            }
        }

        return ShiftAssignmentResult.builder()
                .employeeId(override.getEmployeeId())
                .date(date)
                .workingHours(wh)
                .isWorkingDay(wh != null && !wh.isOff())
                .isOffDay(wh == null || wh.isOff())
                .isOverride(true)
                .isWfh(isWfh != null && isWfh)
                .isOvertimeAllowed(isOvertimeAllowed != null && isOvertimeAllowed)
                .isAttendanceMandatory(isAttendanceMandatory != null && isAttendanceMandatory)
                .overrideNotes(override.getNotes())
                .build();
    }

    private ShiftAssignmentResult buildFromPattern(ShiftPattern pattern, LocalDate date) {
        ShiftPackage pkg = shiftPackageService.getShiftPackageByIdWithWorkingHours(pattern.getShiftPackageId());
        if (pkg == null) {
            return ShiftAssignmentResult.builder()
                    .date(date)
                    .isWorkingDay(false)
                    .isOffDay(true)
                    .build();
        }

        DayOfWeek day = date.getDayOfWeek();
        Long workingHoursId = pkg.getWorkingHoursIdByDay(day);

        WorkingHours wh = null;
        if (workingHoursId != null) {
            wh = workingHoursService.getWorkingHoursById(workingHoursId);
        }

        boolean isOffDay = (wh == null || wh.isOff());

        return ShiftAssignmentResult.builder()
                .date(date)
                .shiftPattern(pattern)
                .shiftPackage(pkg)
                .workingHours(wh)
                .isWorkingDay(!isOffDay)
                .isOffDay(isOffDay)
                .isOverride(false)
                .isWfh(pattern.getIsWfhAllowed() != null && pattern.getIsWfhAllowed())
                .isOvertimeAllowed(pattern.getIsOvertimeAllowed() != null && pattern.getIsOvertimeAllowed())
                .isAttendanceMandatory(pattern.getIsAttendanceMandatory() != null && pattern.getIsAttendanceMandatory())
                .lateToleranceMinutes(pattern.getLateToleranceMinutes())
                .build();
    }

    /**
     * DTO for Shift Assignment Result
     */
    @Data
    @Builder
    public static class ShiftAssignmentResult {
        private Long employeeId;
        private LocalDate date;
        private ShiftPattern shiftPattern;
        private ShiftPackage shiftPackage;
        private WorkingHours workingHours;
        private boolean isWorkingDay;
        private boolean isOffDay;
        private boolean isOverride;
        private boolean isWfh;
        private boolean isOvertimeAllowed;
        private boolean isAttendanceMandatory;
        private Integer lateToleranceMinutes;
        private String overrideNotes;

        /**
         * Check if employee can clock in on this date
         */
        public boolean canClockIn() {
            return isWorkingDay && (isAttendanceMandatory || !isAttendanceMandatory);
        }

        /**
         * Get start time for clock in
         */
        public java.time.LocalTime getStartTime() {
            if (workingHours != null && workingHours.getStartTime() != null) {
                return workingHours.getStartTime();
            }
            return null;
        }

        /**
         * Get end time for clock out
         */
        public java.time.LocalTime getEndTime() {
            if (workingHours != null && workingHours.getEndTime() != null) {
                return workingHours.getEndTime();
            }
            return null;
        }

        /**
         * Check if shift is overnight
         */
        public boolean isOvernight() {
            return workingHours != null && workingHours.getIsOvernight() != null && workingHours.getIsOvernight();
        }
    }
}
