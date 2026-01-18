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
 * Menangani logika bisnis untuk pengajuan cuti
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final ApprovalService approvalService;

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
     * Get pending leave requests for approver
     */
    public List<LeaveRequest> getPendingRequestsForApprover(Long approverId) {
        return leaveRequestRepository.findPendingRequestsForApprover(approverId);
    }

    /**
     * Create new leave request
     */
    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        // Validate dates
        if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        // Check for overlapping leave requests
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaveRequests(
                leaveRequest.getEmployee().getId(),
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
                    leaveRequest.getEmployee().getId(), year);

            if (balanceOpt.isEmpty()) {
                // Initialize leave balance if not exists
                leaveBalanceService.initializeLeaveBalance(
                        leaveRequest.getEmployee().getId(), year);
                balanceOpt = leaveBalanceService.getLeaveBalance(
                        leaveRequest.getEmployee().getId(), year);
            }

            if (!balanceOpt.get().hasSufficientBalance(duration)) {
                throw new IllegalArgumentException(
                        String.format("Insufficient leave balance. Requested: %.1f days, Available: %.1f days",
                                duration, balanceOpt.get().getTotalAvailableBalance())
                );
            }
        }

        // Set current approver
        Employee currentApprover = approvalService.getApprover(
                leaveRequest.getEmployee(),
                leaveRequest
        );
        leaveRequest.setCurrentApprover(currentApprover);

        // Save leave request
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Created leave request {} for employee {} from {} to {}",
                saved.getId(), saved.getEmployee().getId(),
                saved.getStartDate(), saved.getEndDate());

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

    /**
     * Approve leave request
     */
    @Transactional
    public LeaveRequest approveLeaveRequest(Long id, Employee approver) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        if (!leaveRequest.isPending()) {
            throw new IllegalArgumentException(
                    "Leave request is not pending"
            );
        }

        // Check if approver is authorized
        if (!approvalService.canApprove(approver, leaveRequest.getEmployee())) {
            throw new IllegalArgumentException(
                    "You are not authorized to approve this leave request"
            );
        }

        // Approve the request
        leaveRequest.approve(approver);

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
        log.info("Approved leave request {} by {}", id, approver.getId());
        return approved;
    }

    /**
     * Reject leave request
     */
    @Transactional
    public LeaveRequest rejectLeaveRequest(Long id, Employee approver, String reason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));

        if (!leaveRequest.isPending()) {
            throw new IllegalArgumentException(
                    "Leave request is not pending"
            );
        }

        // Check if approver is authorized
        if (!approvalService.canApprove(approver, leaveRequest.getEmployee())) {
            throw new IllegalArgumentException(
                    "You are not authorized to reject this leave request"
            );
        }

        // Reject the request
        leaveRequest.reject(approver, reason);

        LeaveRequest rejected = leaveRequestRepository.save(leaveRequest);
        log.info("Rejected leave request {} by {}. Reason: {}", id, approver.getId(), reason);
        return rejected;
    }

    /**
     * Cancel leave request (by employee)
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
        leaveRequest.setDeletedAt(java.time.LocalDateTime.now());
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
        leaveRequest.setDeletedAt(java.time.LocalDateTime.now());
        leaveRequestRepository.save(leaveRequest);

        log.info("Reimbursed and cancelled leave request {}", id);
        return leaveRequest;
    }

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
        long pending = leaveRequestRepository.countByStatusAndDeletedAtIsNull(LeaveRequestStatus.PENDING);
        long approved = leaveRequestRepository.countByStatusAndDeletedAtIsNull(LeaveRequestStatus.APPROVED);
        long rejected = leaveRequestRepository.countByStatusAndDeletedAtIsNull(LeaveRequestStatus.REJECTED);

        return new LeaveRequestStats(pending, approved, rejected);
    }

    /**
     * DTO for leave request statistics
     */
    public record LeaveRequestStats(
            long pendingCount,
            long approvedCount,
            long rejectedCount
    ) {}
}
