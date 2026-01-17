package com.hris.service;

import com.hris.model.Employee;
import com.hris.model.EmployeeJobHistory;
import com.hris.model.SalaryHistory;
import com.hris.model.enums.SalaryChangeType;
import com.hris.repository.SalaryHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing salary history
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryHistoryService {

    private final SalaryHistoryRepository salaryHistoryRepository;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all salary history for an employee
     */
    public List<SalaryHistory> getEmployeeSalaryHistory(Long employeeId) {
        return salaryHistoryRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }

    /**
     * Get latest salary for an employee
     */
    @Transactional(readOnly = true)
    public SalaryHistory getLatestSalary(Long employeeId) {
        return salaryHistoryRepository.findLatestByEmployeeId(employeeId);
    }

    /**
     * Get salary history by ID
     */
    public SalaryHistory getSalaryHistoryById(Long id) {
        return salaryHistoryRepository.findById(id).orElse(null);
    }

    // =====================================================
    // HISTORY MANAGEMENT
    // =====================================================

    /**
     * Record new salary history entry
     */
    @Transactional
    public SalaryHistory recordSalaryChange(
            Employee employee,
            BigDecimal oldSalary,
            BigDecimal newSalary,
            SalaryChangeType changeType,
            LocalDate effectiveDate,
            String reason,
            Long createdBy,
            EmployeeJobHistory relatedJobHistory) {

        log.info("Recording salary change for employee {} (ID: {}), type: {}, old: {}, new: {}",
                employee.getFullName(), employee.getId(), changeType, oldSalary, newSalary);

        // Mark previous current salary as ended
        SalaryHistory currentSalary = getLatestSalary(employee.getId());
        if (currentSalary != null && currentSalary.getEndDate() == null) {
            currentSalary.setEndDate(effectiveDate.minusDays(1));
            salaryHistoryRepository.save(currentSalary);
            log.info("Ended previous salary history ID: {}", currentSalary.getId());
        }

        SalaryHistory salaryHistory = new SalaryHistory();
        salaryHistory.setEmployee(employee);
        salaryHistory.setOldSalary(oldSalary);
        salaryHistory.setNewSalary(newSalary);

        // Calculate difference
        if (oldSalary != null) {
            salaryHistory.setSalaryDifference(newSalary.subtract(oldSalary));
        } else {
            salaryHistory.setSalaryDifference(newSalary);
        }

        salaryHistory.setChangeType(changeType);
        salaryHistory.setChangeReason(reason);
        salaryHistory.setEffectiveDate(effectiveDate);
        salaryHistory.setCreatedBy(createdBy);
        salaryHistory.setJobHistory(relatedJobHistory);

        return salaryHistoryRepository.save(salaryHistory);
    }

    /**
     * Record initial salary for new hire
     */
    @Transactional
    public SalaryHistory recordInitialSalary(
            Employee employee,
            BigDecimal initialSalary,
            LocalDate hireDate,
            Long createdBy,
            EmployeeJobHistory jobHistory) {

        return recordSalaryChange(
                employee,
                null, // No old salary for new hire
                initialSalary,
                SalaryChangeType.INITIAL,
                hireDate,
                "Initial salary for new hire",
                createdBy,
                jobHistory
        );
    }

    /**
     * Record salary increase
     */
    @Transactional
    public SalaryHistory recordSalaryIncrease(
            Employee employee,
            BigDecimal oldSalary,
            BigDecimal newSalary,
            LocalDate effectiveDate,
            String reason,
            Long createdBy) {

        return recordSalaryChange(
                employee,
                oldSalary,
                newSalary,
                SalaryChangeType.INCREASE,
                effectiveDate,
                reason,
                createdBy,
                null
        );
    }

    /**
     * Record salary decrease
     */
    @Transactional
    public SalaryHistory recordSalaryDecrease(
            Employee employee,
            BigDecimal oldSalary,
            BigDecimal newSalary,
            LocalDate effectiveDate,
            String reason,
            Long createdBy) {

        return recordSalaryChange(
                employee,
                oldSalary,
                newSalary,
                SalaryChangeType.DECREASE,
                effectiveDate,
                reason,
                createdBy,
                null
        );
    }

    /**
     * Record promotion salary adjustment
     */
    @Transactional
    public SalaryHistory recordPromotionSalary(
            Employee employee,
            BigDecimal oldSalary,
            BigDecimal newSalary,
            LocalDate effectiveDate,
            String reason,
            Long createdBy,
            EmployeeJobHistory relatedJobHistory) {

        return recordSalaryChange(
                employee,
                oldSalary,
                newSalary,
                SalaryChangeType.PROMOTION,
                effectiveDate,
                reason,
                createdBy,
                relatedJobHistory
        );
    }

    // =====================================================
    // REPORTS
    // =====================================================

    /**
     * Get salary history by change type
     */
    public List<SalaryHistory> getSalaryHistoryByType(Long employeeId, SalaryChangeType changeType) {
        return salaryHistoryRepository.findByEmployeeIdAndChangeType(employeeId, changeType);
    }

    /**
     * Get salary history within date range
     */
    public List<SalaryHistory> getSalaryHistoryByDateRange(
            Long employeeId, LocalDate startDate, LocalDate endDate) {
        return salaryHistoryRepository.findByEmployeeIdAndEffectiveDateBetween(employeeId, startDate, endDate);
    }

    /**
     * Calculate total salary increase over time
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalSalaryIncrease(Long employeeId) {
        List<SalaryHistory> history = getEmployeeSalaryHistory(employeeId);

        return history.stream()
                .filter(SalaryHistory::isIncrease)
                .map(SalaryHistory::getSalaryDifference)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
