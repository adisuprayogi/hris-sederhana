package com.hris.service;

import com.hris.model.Employee;
import com.hris.model.OvertimeRequest;
import com.hris.model.enums.RequestStatus;
import com.hris.repository.OvertimeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Overtime Request Service
 * Handles overtime request submission and 2-level approval workflow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OvertimeRequestService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final ApprovalService approvalService;
    private final EmployeeService employeeService;

    // =====================================================
    // SUBMIT REQUEST
    // =====================================================

    /**
     * Submit new overtime request
     */
    @Transactional
    public OvertimeRequest submitRequest(Long employeeId, LocalDate requestDate,
                                         BigDecimal estimatedHours, String reason) {
        log.info("Submitting overtime request for employee: {} on {}", employeeId, requestDate);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        // Check if existing request exists for same date
        overtimeRequestRepository.findByEmployeeIdAndRequestDateAndDeletedAtIsNull(employeeId, requestDate)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Overtime request already exists for this date");
                });

        // Determine supervisor
        Employee supervisor = approvalService.determineSupervisor(employee);
        String initialStatus = approvalService.getInitialStatus(employee);

        OvertimeRequest request = OvertimeRequest.builder()
                .employeeId(employeeId)
                .requestDate(requestDate)
                .estimatedHours(estimatedHours)
                .reason(reason)
                .status(RequestStatus.valueOf(initialStatus))
                .supervisor(supervisor)
                .supervisorId(supervisor != null ? supervisor.getId() : null)
                .build();

        OvertimeRequest saved = overtimeRequestRepository.save(request);
        log.info("Overtime request submitted: {}", saved.getId());
        return saved;
    }

    // =====================================================
    // APPROVAL METHODS
    // =====================================================

    /**
     * Approve at supervisor level
     */
    @Transactional
    public OvertimeRequest approveBySupervisor(Long requestId, Long supervisorId, String note) {
        log.info("Approving overtime request {} by supervisor {}", requestId, supervisorId);

        OvertimeRequest request = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found"));

        if (!request.isPendingSupervisor()) {
            throw new IllegalStateException("Request is not pending supervisor approval");
        }

        Employee supervisor = employeeService.getEmployeeById(supervisorId);
        if (supervisor == null) {
            throw new IllegalArgumentException("Supervisor not found");
        }

        request.approveBySupervisor(supervisor, note);
        return overtimeRequestRepository.save(request);
    }

    /**
     * Reject at supervisor level
     */
    @Transactional
    public OvertimeRequest rejectBySupervisor(Long requestId, Long supervisorId, String reason) {
        log.info("Rejecting overtime request {} by supervisor {}", requestId, supervisorId);

        OvertimeRequest request = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found"));

        if (!request.isPendingSupervisor()) {
            throw new IllegalStateException("Request is not pending supervisor approval");
        }

        Employee supervisor = employeeService.getEmployeeById(supervisorId);
        if (supervisor == null) {
            throw new IllegalArgumentException("Supervisor not found");
        }

        request.rejectBySupervisor(supervisor, reason);
        return overtimeRequestRepository.save(request);
    }

    /**
     * Approve at HR level (final approval)
     */
    @Transactional
    public OvertimeRequest approveByHr(Long requestId, Long hrId, String note) {
        log.info("Approving overtime request {} by HR {}", requestId, hrId);

        OvertimeRequest request = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found"));

        if (!request.isPendingHr()) {
            throw new IllegalStateException("Request is not pending HR approval");
        }

        Employee hr = employeeService.getEmployeeById(hrId);
        if (hr == null) {
            throw new IllegalArgumentException("HR not found");
        }

        request.approveByHr(hr, note);
        return overtimeRequestRepository.save(request);
    }

    /**
     * Reject at HR level
     */
    @Transactional
    public OvertimeRequest rejectByHr(Long requestId, Long hrId, String reason) {
        log.info("Rejecting overtime request {} by HR {}", requestId, hrId);

        OvertimeRequest request = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found"));

        if (!request.isPendingHr()) {
            throw new IllegalStateException("Request is not pending HR approval");
        }

        Employee hr = employeeService.getEmployeeById(hrId);
        if (hr == null) {
            throw new IllegalArgumentException("HR not found");
        }

        request.rejectByHr(hr, reason);
        return overtimeRequestRepository.save(request);
    }

    // =====================================================
    // ACTUAL OVERTIME UPDATE
    // =====================================================

    /**
     * Update actual overtime duration after clock out
     */
    @Transactional
    public OvertimeRequest updateActualDuration(Long requestId, Integer actualMinutes) {
        log.info("Updating actual overtime duration for request {}: {} minutes", requestId, actualMinutes);

        OvertimeRequest request = overtimeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime request not found"));

        if (!request.isApproved()) {
            throw new IllegalStateException("Cannot update actual duration for unapproved request");
        }

        request.setActualDuration(actualMinutes);
        return overtimeRequestRepository.save(request);
    }

    // =====================================================
    // QUERY METHODS
    // =====================================================

    /**
     * Get pending supervisor requests
     */
    @Transactional(readOnly = true)
    public List<OvertimeRequest> getPendingSupervisorRequests() {
        return overtimeRequestRepository.findPendingSupervisorRequests();
    }

    /**
     * Get pending HR requests
     */
    @Transactional(readOnly = true)
    public List<OvertimeRequest> getPendingHrRequests() {
        return overtimeRequestRepository.findPendingHrRequests();
    }

    /**
     * Get overtime requests by employee
     */
    @Transactional(readOnly = true)
    public List<OvertimeRequest> getByEmployee(Long employeeId) {
        return overtimeRequestRepository.findByEmployeeIdAndDeletedAtIsNullOrderByRequestDateDesc(employeeId);
    }

    /**
     * Check if employee has approved overtime for specific date
     */
    @Transactional(readOnly = true)
    public boolean hasApprovedOvertimeForDate(Long employeeId, LocalDate date) {
        return overtimeRequestRepository.hasApprovedOvertimeForDate(employeeId, date);
    }
}
