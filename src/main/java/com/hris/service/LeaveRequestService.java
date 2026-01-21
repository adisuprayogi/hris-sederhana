package com.hris.service;

import com.hris.model.Employee;
import com.hris.model.LeaveRequest;
import com.hris.model.enums.LeaveRequestStatus;
import com.hris.model.enums.LeaveType;
import com.hris.repository.LeaveRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk LeaveRequest Entity
 * Menangani logika bisnis untuk pengajuan cuti dengan 2-level approval
 *
 * Approval Flow:
 * 1. PENDING_SUPERVISOR - Waiting for supervisor approval
 * 2. PENDING_HR - Waiting for HR/Admin approval
 * 3. APPROVED - Fully approved
 * 4. REJECTED_BY_SUPERVISOR - Rejected by supervisor
 * 5. REJECTED_BY_HR - Rejected by HR
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final ApprovalService approvalService;
    private final EmployeeService employeeService;

    // =====================================================
    // QUERY METHODS
    // =====================================================

    /**
     * Get all leave requests
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    /**
     * Get leave request by ID
     */
    public Optional<LeaveRequest> getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id);
    }

    /**
     * Get leave requests by employee
     */
    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdAndDeletedAtIsNullOrderByCreatedAtDesc(employeeId);
    }

    /**
     * Get leave requests by status
     */
    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveRequestStatus status) {
        return leaveRequestRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(status);
    }

    /**
     * Get pending supervisor requests (for supervisor approval page)
     */
    public List<LeaveRequest> getPendingSupervisorRequests() {
        return leaveRequestRepository.findPendingSupervisorRequests();
    }

    /**
     * Get pending HR requests (for HR approval page)
     */
    public List<LeaveRequest> getPendingHrRequests() {
        return leaveRequestRepository.findPendingHrRequests();
    }

    // =====================================================
    // CREATE/UPDATE METHODS
    // =====================================================

    /**
     * Create new leave request
     */
    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        Employee employee = leaveRequest.getEmployee();
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Employee is required");
        }

        // Validate dates
        if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        // Check for overlapping leave requests
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaveRequests(
                employee.getId(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException(
                    "Employee already has a leave request during this period"
            );
        }

        // Check balance if leave type requires deduction
        if (leaveBalanceService.requiresBalanceDeduction(leaveRequest.getLeaveType())) {
            double duration = leaveRequest.getDurationDays();
            int year = leaveRequest.getStartDate().getYear();

            // Check if employee has sufficient balance
            var balanceOpt = leaveBalanceService.getLeaveBalance(
                    employee.getId(), year);

            if (balanceOpt.isEmpty()) {
                // Initialize leave balance if not exists
                leaveBalanceService.initializeLeaveBalance(
                        employee.getId(), year);
                balanceOpt = leaveBalanceService.getLeaveBalance(
                        employee.getId(), year);
            }

            if (!balanceOpt.get().hasSufficientBalance(duration)) {
                throw new IllegalArgumentException(
                        String.format("Insufficient leave balance. Requested: %.1f days, Available: %.1f days",
                                duration, balanceOpt.get().getTotalAvailableBalance())
                );
            }
        }

        // Determine initial status based on approval level needed
        Employee requester = employeeService.getEmployeeById(employee.getId());
        String initialStatus = approvalService.getInitialStatus(requester);
        leaveRequest.setStatus(LeaveRequestStatus.valueOf(initialStatus));

        // Save leave request
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Created leave request {} for employee {} from {} to {} with status {}",
                saved.getId(), saved.getEmployee().getId(),
                saved.getStartDate(), saved.getEndDate(), saved.getStatus());

        return saved;
    }

    /**
     * Update leave request
     */
    @Transactional
    public LeaveRequest updateLeaveRequest(Long id, LeaveRequest leaveRequestDetails) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        // Can only update if still pending
        if (!leaveRequest.isPending()) {
            throw new IllegalArgumentException(
                    "Cannot update leave request that is already processed"
            );
        }

        // Update fields
        leaveRequest.setLeaveType(leaveRequestDetails.getLeaveType());
        leaveRequest.setStartDate(leaveRequestDetails.getStartDate());
        leaveRequest.setEndDate(leaveRequestDetails.getEndDate());
        leaveRequest.setReason(leaveRequestDetails.getReason());

        // Re-validate balance if applicable
        if (leaveBalanceService.requiresBalanceDeduction(leaveRequest.getLeaveType())) {
            double duration = leaveRequest.getDurationDays();
            int year = leaveRequest.getStartDate().getYear();

            var balanceOpt = leaveBalanceService.getLeaveBalance(
                    leaveRequest.getEmployee().getId(), year);

            if (balanceOpt.isEmpty() || !balanceOpt.get().hasSufficientBalance(duration)) {
                throw new IllegalArgumentException(
                        "Insufficient leave balance for updated dates"
                );
            }
        }

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        log.info("Updated leave request {}", id);
        return updated;
    }

    // =====================================================
    // APPROVAL METHODS (2-LEVEL)
    // =====================================================

    /**
     * Approve as supervisor
     */
    @Transactional
    public LeaveRequest approveBySupervisor(Long id, Long supervisorId, String note) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING_SUPERVISOR) {
            throw new IllegalArgumentException(
                    "Leave request is not pending supervisor approval"
            );
        }

        Employee supervisor = employeeService.getEmployeeById(supervisorId);
        if (supervisor == null) {
            throw new IllegalArgumentException("Supervisor not found");
        }

        // Validate approver
        if (!approvalService.canApprove(leaveRequest.getEmployee(),
                "PENDING_SUPERVISOR", supervisor)) {
            throw new IllegalArgumentException(
                    "You are not authorized to approve this leave request"
            );
        }

        // Approve at supervisor level (moves to PENDING_HR)
        leaveRequest.approveBySupervisor(supervisor, note);

        LeaveRequest approved = leaveRequestRepository.save(leaveRequest);
        log.info("Approved leave request {} by supervisor {}", id, supervisorId);
        return approved;
    }

    /**
     * Reject as supervisor
     */
    @Transactional
    public LeaveRequest rejectBySupervisor(Long id, Long supervisorId, String reason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING_SUPERVISOR) {
            throw new IllegalArgumentException(
                    "Leave request is not pending supervisor approval"
            );
        }

        Employee supervisor = employeeService.getEmployeeById(supervisorId);
        if (supervisor == null) {
            throw new IllegalArgumentException("Supervisor not found");
        }

        // Validate approver
        if (!approvalService.canApprove(leaveRequest.getEmployee(),
                "PENDING_SUPERVISOR", supervisor)) {
            throw new IllegalArgumentException(
                    "You are not authorized to reject this leave request"
            );
        }

        // Reject at supervisor level
        leaveRequest.rejectBySupervisor(supervisor, reason);

        LeaveRequest rejected = leaveRequestRepository.save(leaveRequest);
        log.info("Rejected leave request {} by supervisor {}. Reason: {}", id, supervisorId, reason);
        return rejected;
    }

    /**
     * Approve as HR (final approval)
     */
    @Transactional
    public LeaveRequest approveByHr(Long id, Long hrId, String note) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING_HR) {
            throw new IllegalArgumentException(
                    "Leave request is not pending HR approval"
            );
        }

        Employee hr = employeeService.getEmployeeById(hrId);
        if (hr == null) {
            throw new IllegalArgumentException("HR not found");
        }

        // Validate approver
        if (!approvalService.canApprove(leaveRequest.getEmployee(),
                "PENDING_HR", hr)) {
            throw new IllegalArgumentException(
                    "You are not authorized to approve this leave request"
            );
        }

        // Approve at HR level (final approval)
        leaveRequest.approveByHr(hr, note);

        // Deduct balance if applicable
        if (leaveBalanceService.requiresBalanceDeduction(leaveRequest.getLeaveType())) {
            double duration = leaveRequest.getDurationDays();
            int year = leaveRequest.getStartDate().getYear();
            leaveBalanceService.deductBalance(
                    leaveRequest.getEmployee().getId(),
                    year,
                    duration
            );
            log.info("Deducted {} days from leave balance for employee {}",
                    duration, leaveRequest.getEmployee().getId());
        }

        LeaveRequest approved = leaveRequestRepository.save(leaveRequest);
        log.info("Approved leave request {} by HR {}", id, hrId);
        return approved;
    }

    /**
     * Reject as HR
     */
    @Transactional
    public LeaveRequest rejectByHr(Long id, Long hrId, String reason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING_HR) {
            throw new IllegalArgumentException(
                    "Leave request is not pending HR approval"
            );
        }

        Employee hr = employeeService.getEmployeeById(hrId);
        if (hr == null) {
            throw new IllegalArgumentException("HR not found");
        }

        // Validate approver
        if (!approvalService.canApprove(leaveRequest.getEmployee(),
                "PENDING_HR", hr)) {
            throw new IllegalArgumentException(
                    "You are not authorized to reject this leave request"
            );
        }

        // Reject at HR level
        leaveRequest.rejectByHr(hr, reason);

        LeaveRequest rejected = leaveRequestRepository.save(leaveRequest);
        log.info("Rejected leave request {} by HR {}. Reason: {}", id, hrId, reason);
        return rejected;
    }

    // =====================================================
    // CANCEL METHODS
    // =====================================================

    /**
     * Cancel leave request (by employee) - soft delete
     */
    @Transactional
    public LeaveRequest cancelLeaveRequest(Long id, Employee employee) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        // Can only cancel own request
        if (!leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException(
                    "You can only cancel your own leave requests"
            );
        }

        // Can only cancel pending requests
        if (!leaveRequest.isPending()) {
            throw new IllegalArgumentException(
                    "Cannot cancel leave request that is already processed"
            );
        }

        // Soft delete
        leaveRequest.setDeletedAt(LocalDateTime.now());
        leaveRequestRepository.save(leaveRequest);

        log.info("Cancelled leave request {} by employee {}", id, employee.getId());
        return leaveRequest;
    }

    /**
     * Reimburse leave balance (for approved leaves that are cancelled)
     */
    @Transactional
    public LeaveRequest reimburseLeaveRequest(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        if (!leaveRequest.isApproved()) {
            throw new IllegalArgumentException(
                    "Can only reimburse approved leave requests"
            );
        }

        // Reimburse balance if applicable
        if (leaveBalanceService.requiresBalanceDeduction(leaveRequest.getLeaveType())) {
            double duration = leaveRequest.getDurationDays();
            int year = leaveRequest.getStartDate().getYear();
            leaveBalanceService.reimburseBalance(
                    leaveRequest.getEmployee().getId(),
                    year,
                    duration
            );
            log.info("Reimbursed {} days to leave balance for employee {}",
                    duration, leaveRequest.getEmployee().getId());
        }

        // Soft delete
        leaveRequest.setDeletedAt(LocalDateTime.now());
        leaveRequestRepository.save(leaveRequest);

        log.info("Reimbursed and cancelled leave request {}", id);
        return leaveRequest;
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    /**
     * Check if employee is on leave on specific date
     */
    public boolean isEmployeeOnLeave(Long employeeId, LocalDate date) {
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeaveInDateRange(
                employeeId, date, date);

        return !approvedLeaves.isEmpty();
    }

    /**
     * Get leave statistics
     */
    public LeaveRequestStats getLeaveRequestStats() {
        long pendingSupervisor = leaveRequestRepository.countByStatusAndDeletedAtIsNull(
                LeaveRequestStatus.PENDING_SUPERVISOR);
        long pendingHr = leaveRequestRepository.countByStatusAndDeletedAtIsNull(
                LeaveRequestStatus.PENDING_HR);
        long approved = leaveRequestRepository.countByStatusAndDeletedAtIsNull(
                LeaveRequestStatus.APPROVED);
        long rejectedSupervisor = leaveRequestRepository.countByStatusAndDeletedAtIsNull(
                LeaveRequestStatus.REJECTED_BY_SUPERVISOR);
        long rejectedHr = leaveRequestRepository.countByStatusAndDeletedAtIsNull(
                LeaveRequestStatus.REJECTED_BY_HR);

        return new LeaveRequestStats(pendingSupervisor, pendingHr, approved,
                rejectedSupervisor, rejectedHr);
    }

    /**
     * DTO for leave request statistics
     */
    public record LeaveRequestStats(
            long pendingSupervisorCount,
            long pendingHrCount,
            long approvedCount,
            long rejectedBySupervisorCount,
            long rejectedByHrCount
    ) {}
}
