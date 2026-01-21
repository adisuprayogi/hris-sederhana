package com.hris.service;

import com.hris.model.Employee;
import com.hris.model.WfhRequest;
import com.hris.model.enums.RequestStatus;
import com.hris.repository.WfhRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * WFH Request Service
 * Handles WFH request submission and 2-level approval workflow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfhRequestService {

    private final WfhRequestRepository wfhRequestRepository;
    private final ApprovalService approvalService;
    private final EmployeeService employeeService;

    // =====================================================
    // SUBMIT REQUEST
    // =====================================================

    /**
     * Submit new WFH request
     */
    @Transactional
    public WfhRequest submitRequest(Long employeeId, LocalDate requestDate, String reason) {
        log.info("Submitting WFH request for employee: {} on {}", employeeId, requestDate);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        // Check if existing request exists for same date
        wfhRequestRepository.findByEmployeeIdAndRequestDateAndDeletedAtIsNull(employeeId, requestDate)
                .ifPresent(existing -> {
                    throw new IllegalStateException("WFH request already exists for this date");
                });

        // Determine supervisor
        Employee supervisor = approvalService.determineSupervisor(employee);
        String initialStatus = approvalService.getInitialStatus(employee);

        WfhRequest request = WfhRequest.builder()
                .employeeId(employeeId)
                .requestDate(requestDate)
                .reason(reason)
                .status(RequestStatus.valueOf(initialStatus))
                .supervisor(supervisor)
                .supervisorId(supervisor != null ? supervisor.getId() : null)
                .build();

        WfhRequest saved = wfhRequestRepository.save(request);
        log.info("WFH request submitted: {}", saved.getId());
        return saved;
    }

    // =====================================================
    // APPROVAL METHODS
    // =====================================================

    /**
     * Approve at supervisor level
     */
    @Transactional
    public WfhRequest approveBySupervisor(Long requestId, Long supervisorId, String note) {
        log.info("Approving WFH request {} by supervisor {}", requestId, supervisorId);

        WfhRequest request = wfhRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("WFH request not found"));

        if (!request.isPendingSupervisor()) {
            throw new IllegalStateException("Request is not pending supervisor approval");
        }

        Employee supervisor = employeeService.getEmployeeById(supervisorId);
        if (supervisor == null) {
            throw new IllegalArgumentException("Supervisor not found");
        }

        request.approveBySupervisor(supervisor, note);
        return wfhRequestRepository.save(request);
    }

    /**
     * Reject at supervisor level
     */
    @Transactional
    public WfhRequest rejectBySupervisor(Long requestId, Long supervisorId, String reason) {
        log.info("Rejecting WFH request {} by supervisor {}", requestId, supervisorId);

        WfhRequest request = wfhRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("WFH request not found"));

        if (!request.isPendingSupervisor()) {
            throw new IllegalStateException("Request is not pending supervisor approval");
        }

        Employee supervisor = employeeService.getEmployeeById(supervisorId);
        if (supervisor == null) {
            throw new IllegalArgumentException("Supervisor not found");
        }

        request.rejectBySupervisor(supervisor, reason);
        return wfhRequestRepository.save(request);
    }

    /**
     * Approve at HR level (final approval)
     */
    @Transactional
    public WfhRequest approveByHr(Long requestId, Long hrId, String note) {
        log.info("Approving WFH request {} by HR {}", requestId, hrId);

        WfhRequest request = wfhRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("WFH request not found"));

        if (!request.isPendingHr()) {
            throw new IllegalStateException("Request is not pending HR approval");
        }

        Employee hr = employeeService.getEmployeeById(hrId);
        if (hr == null) {
            throw new IllegalArgumentException("HR not found");
        }

        request.approveByHr(hr, note);
        return wfhRequestRepository.save(request);
    }

    /**
     * Reject at HR level
     */
    @Transactional
    public WfhRequest rejectByHr(Long requestId, Long hrId, String reason) {
        log.info("Rejecting WFH request {} by HR {}", requestId, hrId);

        WfhRequest request = wfhRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("WFH request not found"));

        if (!request.isPendingHr()) {
            throw new IllegalStateException("Request is not pending HR approval");
        }

        Employee hr = employeeService.getEmployeeById(hrId);
        if (hr == null) {
            throw new IllegalArgumentException("HR not found");
        }

        request.rejectByHr(hr, reason);
        return wfhRequestRepository.save(request);
    }

    // =====================================================
    // QUERY METHODS
    // =====================================================

    /**
     * Get pending supervisor requests
     */
    @Transactional(readOnly = true)
    public List<WfhRequest> getPendingSupervisorRequests() {
        return wfhRequestRepository.findPendingSupervisorRequests();
    }

    /**
     * Get pending HR requests
     */
    @Transactional(readOnly = true)
    public List<WfhRequest> getPendingHrRequests() {
        return wfhRequestRepository.findPendingHrRequests();
    }

    /**
     * Get WFH requests by employee
     */
    @Transactional(readOnly = true)
    public List<WfhRequest> getByEmployee(Long employeeId) {
        return wfhRequestRepository.findByEmployeeIdAndDeletedAtIsNullOrderByRequestDateDesc(employeeId);
    }

    /**
     * Check if employee has approved WFH for specific date
     */
    @Transactional(readOnly = true)
    public boolean hasApprovedWfhForDate(Long employeeId, LocalDate date) {
        return wfhRequestRepository.hasApprovedWfhForDate(employeeId, date);
    }
}
