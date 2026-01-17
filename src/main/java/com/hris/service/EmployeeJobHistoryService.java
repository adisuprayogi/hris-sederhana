package com.hris.service;

import com.hris.model.Department;
import com.hris.model.Employee;
import com.hris.model.EmployeeJobHistory;
import com.hris.model.Position;
import com.hris.model.enums.ChangeType;
import com.hris.repository.EmployeeJobHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing employee job history
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeJobHistoryService {

    private final EmployeeJobHistoryRepository jobHistoryRepository;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all job history for an employee
     */
    public List<EmployeeJobHistory> getEmployeeJobHistory(Long employeeId) {
        return jobHistoryRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }

    /**
     * Get current job for an employee
     */
    @Transactional(readOnly = true)
    public EmployeeJobHistory getCurrentJob(Long employeeId) {
        return jobHistoryRepository.findCurrentByEmployeeId(employeeId);
    }

    /**
     * Get job history by ID
     */
    public EmployeeJobHistory getJobHistoryById(Long id) {
        return jobHistoryRepository.findById(id).orElse(null);
    }

    // =====================================================
    // HISTORY MANAGEMENT
    // =====================================================

    /**
     * Record new job history entry
     */
    @Transactional
    public EmployeeJobHistory recordJobHistory(
            Employee employee,
            Department department,
            Position position,
            ChangeType changeType,
            LocalDate effectiveDate,
            String reason,
            BigDecimal salaryAtTime) {

        log.info("Recording job history for employee {} (ID: {}), type: {}",
                employee.getFullName(), employee.getId(), changeType);

        // Mark previous current job as not current
        EmployeeJobHistory currentJob = jobHistoryRepository.findCurrentByEmployeeId(employee.getId());
        if (currentJob != null) {
            currentJob.setIsCurrent(false);
            currentJob.setEndDate(effectiveDate.minusDays(1));
            jobHistoryRepository.save(currentJob);
            log.info("Ended previous job history ID: {}", currentJob.getId());
        }

        // Create new job history entry
        EmployeeJobHistory jobHistory = new EmployeeJobHistory();
        jobHistory.setEmployee(employee);
        jobHistory.setDepartment(department);
        jobHistory.setPosition(position);
        jobHistory.setChangeType(changeType);
        jobHistory.setEffectiveDate(effectiveDate);
        jobHistory.setChangeReason(reason);
        jobHistory.setSalaryAtTime(salaryAtTime);
        jobHistory.setIsCurrent(true);

        EmployeeJobHistory saved = jobHistoryRepository.save(jobHistory);
        log.info("Created new job history ID: {}", saved.getId());

        return saved;
    }

    /**
     * Record job history for new hire
     */
    @Transactional
    public EmployeeJobHistory recordNewHire(
            Employee employee,
            Department department,
            Position position,
            LocalDate hireDate,
            BigDecimal salary) {

        return recordJobHistory(
                employee,
                department,
                position,
                ChangeType.INITIAL,
                hireDate,
                "New Hire",
                salary
        );
    }

    /**
     * Record job history for promotion
     */
    @Transactional
    public EmployeeJobHistory recordPromotion(
            Employee employee,
            Position newPosition,
            LocalDate effectiveDate,
            String reason,
            BigDecimal newSalary) {

        return recordJobHistory(
                employee,
                employee.getDepartment(),
                newPosition,
                ChangeType.PROMOTION,
                effectiveDate,
                reason,
                newSalary
        );
    }

    /**
     * Record job history for transfer
     */
    @Transactional
    public EmployeeJobHistory recordTransfer(
            Employee employee,
            Department newDepartment,
            LocalDate effectiveDate,
            String reason,
            BigDecimal salary) {

        return recordJobHistory(
                employee,
                newDepartment,
                employee.getPosition(),
                ChangeType.TRANSFER,
                effectiveDate,
                reason,
                salary
        );
    }

    /**
     * Record job history for resignation
     */
    @Transactional
    public EmployeeJobHistory recordResignation(
            Employee employee,
            LocalDate resignationDate,
            String reason) {

        // Mark current job as ended
        EmployeeJobHistory currentJob = jobHistoryRepository.findCurrentByEmployeeId(employee.getId());
        if (currentJob != null) {
            currentJob.setIsCurrent(false);
            currentJob.setEndDate(resignationDate);
            jobHistoryRepository.save(currentJob);
        }

        // Create resignation record
        EmployeeJobHistory jobHistory = new EmployeeJobHistory();
        jobHistory.setEmployee(employee);
        jobHistory.setDepartment(employee.getDepartment());
        jobHistory.setPosition(employee.getPosition());
        jobHistory.setChangeType(ChangeType.RESIGNATION);
        jobHistory.setEffectiveDate(resignationDate);
        jobHistory.setChangeReason(reason);
        jobHistory.setIsCurrent(false);
        jobHistory.setEndDate(resignationDate);

        return jobHistoryRepository.save(jobHistory);
    }

    // =====================================================
    // REPORTS
    // =====================================================

    /**
     * Get job history by change type
     */
    public List<EmployeeJobHistory> getJobHistoryByType(Long employeeId, ChangeType changeType) {
        return jobHistoryRepository.findByEmployeeIdAndChangeType(employeeId, changeType);
    }

    /**
     * Get job history within date range
     */
    public List<EmployeeJobHistory> getJobHistoryByDateRange(
            Long employeeId, LocalDate startDate, LocalDate endDate) {
        return jobHistoryRepository.findByEmployeeIdAndEffectiveDateBetween(employeeId, startDate, endDate);
    }

    /**
     * Get total duration in company (from first hire to now)
     */
    @Transactional(readOnly = true)
    public long getTotalTenureDays(Long employeeId) {
        List<EmployeeJobHistory> history = getEmployeeJobHistory(employeeId);
        if (history.isEmpty()) {
            return 0;
        }

        EmployeeJobHistory firstJob = history.get(history.size() - 1); // Oldest record
        LocalDate startDate = firstJob.getEffectiveDate();
        LocalDate endDate = LocalDate.now();

        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }
}
