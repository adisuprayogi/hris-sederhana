package com.hris.service;

import com.hris.model.Employee;
import com.hris.model.Position;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Approval Service
 * Handles 2-level approval workflow logic
 *
 * Determines supervisor (atasan langsung) based on:
 * 1. Department hierarchy (parent/child relationship)
 * 2. Position level
 * 3. Head of department assignments
 *
 * Special cases:
 * - Rektor (position level 6) → No supervisor (1-level: HR only)
 * - Head of Root Department → No supervisor (1-level: HR only)
 * - Head of Child Department → Parent department head is supervisor
 * - Regular employee → Current department head is supervisor
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final EmployeeService employeeService;

    // =====================================================
    // SUPERVISOR DETERMINATION
    // =====================================================

    /**
     * Determine supervisor for requester
     * Based on department hierarchy and position level
     *
     * @param requester Employee who is making the request
     * @return Supervisor employee, or null if no supervisor needed (highest level)
     */
    public Employee determineSupervisor(Employee requester) {
        if (requester == null) {
            return null;
        }

        Position requesterPosition = requester.getPosition();
        var requesterDept = requester.getDepartment();

        // Case 1: Rektor (position level 6) → No supervisor
        if (requesterPosition != null && requesterPosition.getLevel() >= 6) {
            log.info("Requester is Rektor (level 6), no supervisor needed");
            return null;
        }

        if (requesterDept == null) {
            log.warn("Requester has no department, no supervisor determined");
            return null;
        }

        // Case 2: Head of Root Department → No supervisor
        if (requesterDept.isRoot() && isHeadOfDepartment(requester, requesterDept)) {
            log.info("Requester is head of root department, no supervisor needed");
            return null;
        }

        // Case 3: Head of Child Department → Parent department head
        if (isHeadOfDepartment(requester, requesterDept) && !requesterDept.isRoot()) {
            var parentDept = requesterDept.getParent();
            if (parentDept != null && parentDept.getHead() != null) {
                Employee parentHead = parentDept.getHead();
                // Make sure parent head is not the same as requester
                if (parentHead != null && !parentHead.getId().equals(requester.getId())) {
                    log.info("Requester is head of child department, supervisor is parent dept head: {}",
                            parentHead.getFullName());
                    return parentHead;
                }
            }
        }

        // Case 4: Regular employee → Current department head
        Employee deptHead = requesterDept.getHead();
        if (deptHead != null && !deptHead.getId().equals(requester.getId())) {
            log.info("Requester is regular employee, supervisor is dept head: {}", deptHead.getFullName());
            return deptHead;
        }

        // Case 5: No valid supervisor found
        log.warn("No valid supervisor found for requester: {}", requester.getFullName());
        return null;
    }

    /**
     * Check if requester needs 2-level approval
     *
     * @param requester Employee who is making the request
     * @return true if needs supervisor approval, false if directly to HR
     */
    public boolean needsTwoLevelApproval(Employee requester) {
        return determineSupervisor(requester) != null;
    }

    /**
     * Get initial approval status for a new request
     *
     * @param requester Employee who is making the request
     * @return "PENDING_SUPERVISOR" if needs 2-level, "PENDING_HR" if only 1-level
     */
    public String getInitialStatus(Employee requester) {
        return needsTwoLevelApproval(requester) ? "PENDING_SUPERVISOR" : "PENDING_HR";
    }

    /**
     * Get supervisor ID for requester (for database storage)
     * Returns null if no supervisor needed
     */
    public Long getSupervisorId(Employee requester) {
        Employee supervisor = determineSupervisor(requester);
        return supervisor != null ? supervisor.getId() : null;
    }

    /**
     * Validate if the given approver can approve the request at current status
     *
     * @param requester Employee who made the request
     * @param currentStatus Current status of the request
     * @param approverEmployee Employee trying to approve
     * @return true if approver can approve, false otherwise
     */
    public boolean canApprove(Employee requester, String currentStatus, Employee approverEmployee) {
        if (requester == null || approverEmployee == null) {
            return false;
        }

        // Cannot approve own request
        if (requester.getId().equals(approverEmployee.getId())) {
            log.warn("Approver attempted to approve own request");
            return false;
        }

        Employee expectedSupervisor = determineSupervisor(requester);

        // If current status is PENDING_SUPERVISOR
        if ("PENDING_SUPERVISOR".equals(currentStatus)) {
            if (expectedSupervisor == null) {
                // No supervisor needed, HR can approve directly
                return isHrOrAdmin(approverEmployee);
            }
            // Check if approver is the expected supervisor
            return expectedSupervisor.getId().equals(approverEmployee.getId());
        }

        // If current status is PENDING_HR
        if ("PENDING_HR".equals(currentStatus)) {
            // Only HR/Admin can approve
            return isHrOrAdmin(approverEmployee);
        }

        return false;
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if employee is the head of the specified department
     */
    private boolean isHeadOfDepartment(Employee employee, com.hris.model.Department department) {
        if (department == null || department.getHead() == null) {
            return false;
        }
        return department.getHead().getId().equals(employee.getId());
    }

    /**
     * Check if employee has HR or Admin role
     * TODO: Implement proper role checking when User/Role system is ready
     */
    private boolean isHrOrAdmin(Employee employee) {
        // For now, return true - will be implemented with proper role system
        // Should check if employee has HR or ADMIN role
        return true;
    }
}
