package com.hris.service;

import com.hris.model.Employee;
import com.hris.model.LeaveBalance;
import com.hris.model.enums.LeaveType;
import com.hris.repository.EmployeeRepository;
import com.hris.repository.LeaveBalanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk LeaveBalance Entity
 * Menangani pengelolaan saldo cuti karyawan
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Get leave balance for employee in specific year
     */
    public Optional<LeaveBalance> getLeaveBalance(Long employeeId, Integer year) {
        return leaveBalanceRepository.findByEmployeeIdAndYearAndDeletedAtIsNull(employeeId, year);
    }

    /**
     * Get all leave balances for a specific year
     */
    public List<LeaveBalance> getAllLeaveBalancesForYear(Integer year) {
        return leaveBalanceRepository.findByYearAndDeletedAtIsNullOrderByEmployeeFullNameAsc(year);
    }

    /**
     * Search leave balances by employee name
     */
    public List<LeaveBalance> searchLeaveBalancesByName(String name, Integer year) {
        return leaveBalanceRepository.searchByEmployeeNameAndYear(name, year);
    }

    /**
     * Get leave balances by department
     */
    public List<LeaveBalance> getLeaveBalancesByDepartment(Long departmentId, Integer year) {
        return leaveBalanceRepository.findByDepartmentAndYear(departmentId, year);
    }

    /**
     * Get current year leave balance for employee
     */
    public LeaveBalance getCurrentLeaveBalance(Long employeeId) {
        int currentYear = LocalDate.now().getYear();
        return leaveBalanceRepository.findByEmployeeIdAndYearAndDeletedAtIsNull(employeeId, currentYear)
                .orElseGet(() -> initializeLeaveBalance(employeeId, currentYear));
    }

    /**
     * Get all leave balances for employee
     */
    public List<LeaveBalance> getEmployeeLeaveBalances(Long employeeId) {
        return leaveBalanceRepository.findByEmployeeIdAndDeletedAtIsNullOrderByYearDesc(employeeId);
    }

    /**
     * Initialize leave balance for new employee
     */
    @Transactional
    public LeaveBalance initializeLeaveBalance(Long employeeId, Integer year) {
        if (leaveBalanceRepository.existsByEmployeeIdAndYearAndDeletedAtIsNull(employeeId, year)) {
            throw new IllegalArgumentException(
                    "Leave balance already exists for employee " + employeeId + " in year " + year
            );
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(employee);
        leaveBalance.setYear(year);
        leaveBalance.setAnnualQuota(12); // Default 12 days per year
        leaveBalance.setBalance(12.0); // Full balance at start
        leaveBalance.setUsed(0.0);
        leaveBalance.setCarriedForward(0.0);
        leaveBalance.setExpiredBalance(0.0);
        leaveBalance.setTotalDeduction(0.0);

        LeaveBalance saved = leaveBalanceRepository.save(leaveBalance);
        log.info("Initialized leave balance for employee {} in year {}", employeeId, year);
        return saved;
    }

    /**
     * Create or update leave balance
     */
    @Transactional
    public LeaveBalance saveLeaveBalance(LeaveBalance leaveBalance) {
        return leaveBalanceRepository.save(leaveBalance);
    }

    /**
     * Deduct balance for leave request
     */
    @Transactional
    public void deductBalance(Long employeeId, Integer year, double days) {
        LeaveBalance leaveBalance = getCurrentLeaveBalance(employeeId);

        if (!leaveBalance.hasSufficientBalance(days)) {
            throw new IllegalArgumentException(
                    String.format("Insufficient leave balance. Requested: %.1f, Available: %.1f",
                            days, leaveBalance.getTotalAvailableBalance())
            );
        }

        leaveBalance.deductBalance(days);
        leaveBalanceRepository.save(leaveBalance);
        log.info("Deducted {} days from leave balance for employee {} in year {}", days, employeeId, year);
    }

    /**
     * Reimburse leave balance (for cancelled leave requests)
     */
    @Transactional
    public void reimburseBalance(Long employeeId, Integer year, double days) {
        LeaveBalance leaveBalance = getCurrentLeaveBalance(employeeId);
        leaveBalance.addBalance(days);
        leaveBalanceRepository.save(leaveBalance);
        log.info("Reimbursed {} days to leave balance for employee {} in year {}", days, employeeId, year);
    }

    /**
     * Reset leave balance for new year with carry forward
     * According to PP No. 35 Tahun 2021, unused leave can be carried forward but expires after 6 months
     */
    @Transactional
    public void resetLeaveBalanceForNewYear(Long employeeId, int currentYear) {
        int previousYear = currentYear - 1;
        Optional<LeaveBalance> previousBalanceOpt = leaveBalanceRepository
                .findByEmployeeIdAndYearAndDeletedAtIsNull(employeeId, previousYear);

        if (previousBalanceOpt.isEmpty()) {
            // No previous balance, just create new one
            initializeLeaveBalance(employeeId, currentYear);
            return;
        }

        LeaveBalance previousBalance = previousBalanceOpt.get();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        LeaveBalance newBalance = new LeaveBalance();
        newBalance.setEmployee(employee);
        newBalance.setYear(currentYear);
        newBalance.setAnnualQuota(previousBalance.getAnnualQuota());

        // Calculate carried forward
        double unusedPreviousYear = previousBalance.getRemainingBalance();
        double maxCarryForward = previousBalance.getAnnualQuota() / 2.0; // Half of annual quota
        double actualCarryForward = Math.min(unusedPreviousYear, maxCarryForward);
        double expired = Math.max(0, unusedPreviousYear - maxCarryForward);

        newBalance.setCarriedForward(actualCarryForward);
        newBalance.setCarriedForwardExpiryDate(LocalDate.of(currentYear, 6, 30)); // Expires on June 30
        newBalance.setExpiredBalance(expired);
        newBalance.setBalance((double) previousBalance.getAnnualQuota() + actualCarryForward);
        newBalance.setUsed(0.0);
        newBalance.setTotalDeduction(0.0);

        leaveBalanceRepository.save(newBalance);
        log.info("Reset leave balance for employee {} for year {}. Carried forward: {}, Expired: {}",
                employeeId, currentYear, actualCarryForward, expired);
    }

    /**
     * Check if leave type requires balance deduction
     */
    public boolean requiresBalanceDeduction(LeaveType leaveType) {
        return leaveType.isDeductingFromBalance();
    }

    /**
     * Calculate leave days between two dates
     */
    public double calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        return startDate.datesUntil(endDate.plusDays(1)).count();
    }

    /**
     * Calculate actual leave days excluding holidays
     */
    public double calculateActualLeaveDays(LocalDate startDate, LocalDate endDate, boolean excludeHolidays) {
        if (!excludeHolidays) {
            return calculateLeaveDays(startDate, endDate);
        }

        // TODO: Implement holiday exclusion using HolidayService
        return calculateLeaveDays(startDate, endDate);
    }

    /**
     * Get leave balance statistics
     */
    public LeaveBalanceStats getLeaveBalanceStats(Long employeeId) {
        LeaveBalance currentBalance = getCurrentLeaveBalance(employeeId);

        return new LeaveBalanceStats(
                currentBalance.getAnnualQuota(),
                currentBalance.getBalance(),
                currentBalance.getUsed(),
                currentBalance.getCarriedForward() != null ? currentBalance.getCarriedForward() : 0.0,
                currentBalance.getExpiredBalance() != null ? currentBalance.getExpiredBalance() : 0.0,
                currentBalance.getRemainingBalance(),
                currentBalance.getTotalAvailableBalance(),
                currentBalance.getUtilizationPercentage()
        );
    }

    /**
     * Check for expiring carried forward balances and process them
     */
    @Transactional
    public void processExpiredCarriedForwardBalances() {
        List<LeaveBalance> expiringBalances = leaveBalanceRepository.findExpiringCarriedForwardBalances();
        LocalDate today = LocalDate.now();

        for (LeaveBalance balance : expiringBalances) {
            if (balance.isCarriedForwardExpired()) {
                double expiredAmount = balance.getCarriedForward();
                balance.expireCarriedForward();
                leaveBalanceRepository.save(balance);
                log.info("Expired {} carried forward days for employee {} in year {}",
                        expiredAmount, balance.getEmployee().getId(), balance.getYear());
            }
        }
    }

    /**
     * Get employees with low leave balance
     */
    public List<LeaveBalance> getLowBalanceEmployees(double threshold) {
        // Return balances with remaining less than threshold
        return leaveBalanceRepository.findAll().stream()
                .filter(lb -> lb.getDeletedAt() == null)
                .filter(lb -> lb.getRemainingBalance() < threshold)
                .toList();
    }

    /**
     * Adjust leave balance manually (for HR/Admin)
     */
    @Transactional
    public LeaveBalance adjustLeaveBalance(Long balanceId, double adjustment, String reason) {
        LeaveBalance balance = leaveBalanceRepository.findById(balanceId)
                .orElseThrow(() -> new IllegalArgumentException("Leave balance not found with id: " + balanceId));

        double newBalance = balance.getBalance() + adjustment;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        balance.setBalance(newBalance);
        if (balance.getNotes() == null) {
            balance.setNotes(reason);
        } else {
            balance.setNotes(balance.getNotes() + "; " + reason);
        }

        LeaveBalance saved = leaveBalanceRepository.save(balance);
        log.info("Adjusted leave balance {} by {}. New balance: {}", balanceId, adjustment, newBalance);
        return saved;
    }

    /**
     * DTO for leave balance statistics
     */
    public record LeaveBalanceStats(
            int annualQuota,
            double totalBalance,
            double used,
            double carriedForward,
            double expired,
            double remaining,
            double totalAvailable,
            double utilizationPercentage
    ) {}
}
