package com.hris.service;

import com.hris.model.Department;
import com.hris.model.Employee;
import com.hris.model.LeaveRequest;
import com.hris.model.enums.LeaveRequestStatus;
import com.hris.repository.DepartmentRepository;
import com.hris.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Approval Service
 * Handles approval logic for leave requests and attendance
 * Uses hierarchical department structure to determine appropriate approvers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    // =====================================================
    // APPROVER DETERMINATION
    // =====================================================

    /**
     * Get the appropriate approver for a leave request based on:
     * 1. Requester's position in the organizational hierarchy
     * 2. Duration of the leave (short vs long)
     * 3. Department structure
     *
     * Approval Logic:
     * - Staff (level 1-3):
     *   - 1-3 days: Department Head
     *   - >3 days: Department Head -> Parent Dept Head -> HR
     *
     * - Manager (level 4):
     *   - 1-3 days: Parent Department Head
     *   - >3 days: Parent Dept Head -> HR -> (Warek/Rektor if needed)
     *
     * - Dept Head (level 5):
     *   - Any duration: Parent Dept Head -> HR
     *
     * - Warek/Rektor (level 6):
     *   - Any duration: HR -> Peer/Board
     *
     * @param requester The employee requesting leave
     * @param leaveRequest The leave request
     * @return The appropriate approver
     */
    public Employee getApprover(Employee requester, LeaveRequest leaveRequest) {
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        log.debug("Determining approver for requester: {} (ID: {}), leave duration: {} days",
                requester.getFullName(), requester.getId(), getLeaveDurationDays(leaveRequest));

        // Check if requester is department head
        boolean isDeptHead = isDepartmentHead(requester);

        // Get leave duration
        long durationDays = getLeaveDurationDays(leaveRequest);
        boolean isShortLeave = durationDays <= 3;

        // Get position level
        int positionLevel = requester.getPosition() != null ? requester.getPosition().getLevel() : 1;

        // Determine approver based on hierarchy
        Employee approver = null;

        if (isDeptHead) {
            // Department head: get parent department head
            approver = getParentDepartmentHead(requester.getDepartment(), requester);
        } else if (positionLevel >= 4) {
            // Manager level: get parent department head directly
            approver = getParentDepartmentHead(requester.getDepartment(), requester);
        } else {
            // Regular staff: get direct department head
            approver = getDepartmentHead(requester.getDepartment(), requester);
        }

        // Fallback to direct approver if set
        if (approver == null && requester.getApprover() != null) {
            approver = requester.getApprover();
        }

        // Final fallback to HR admin
        if (approver == null) {
            approver = getHRUser();
            log.warn("No approver found for requester {}, using HR fallback", requester.getEmail());
        }

        log.debug("Selected approver: {} (ID: {})", approver.getFullName(), approver.getId());
        return approver;
    }

    /**
     * Get the next approver in the approval chain
     * Used when current approver has approved but more approval is needed
     *
     * @param currentApprover The current approver
     * @param leaveRequest The leave request
     * @return The next approver in chain, or null if approval is complete
     */
    public Employee getNextApprover(Employee currentApprover, LeaveRequest leaveRequest) {
        Employee requester = leaveRequest.getEmployee();
        long durationDays = getLeaveDurationDays(leaveRequest);

        // If short leave, no further approval needed after first approver
        if (durationDays <= 3) {
            return null;
        }

        // For long leave, get parent department head or HR
        Department currentDept = currentApprover.getDepartment();
        if (currentDept != null && currentDept.getParent() != null) {
            Employee parentHead = currentDept.getParent().getHead();
            if (parentHead != null && !parentHead.getId().equals(currentApprover.getId())) {
                return parentHead;
            }
        }

        // Final step: HR approval
        Employee hrUser = getHRUser();
        if (!hrUser.getId().equals(currentApprover.getId())) {
            return hrUser;
        }

        return null; // Approval chain complete
    }

    /**
     * Check if an approver can approve a request from a requester
     * Prevents self-approval and validates organizational hierarchy
     *
     * @param approver The potential approver
     * @param requester The employee requesting leave
     * @return true if the approver can approve this request
     */
    public boolean canApprove(Employee approver, Employee requester) {
        // Cannot approve own request
        if (approver.getId().equals(requester.getId())) {
            log.warn("Approver {} attempted to approve own request", approver.getEmail());
            return false;
        }

        // Check if approver is department head of requester's department
        if (isDepartmentHeadOf(approver, requester.getDepartment())) {
            return true;
        }

        // Check if approver is parent department head
        if (isParentDepartmentHeadOf(approver, requester.getDepartment())) {
            return true;
        }

        // Check if approver is HR
        if (isHR(approver)) {
            return true;
        }

        // Check if approver is explicitly set as requester's approver
        if (requester.getApprover() != null &&
            requester.getApprover().getId().equals(approver.getId())) {
            return true;
        }

        log.warn("Approver {} is not authorized to approve requests from {}",
                approver.getEmail(), requester.getEmail());
        return false;
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if an employee is a department head
     */
    public boolean isDepartmentHead(Employee employee) {
        if (employee == null || employee.getDepartment() == null) {
            return false;
        }

        Department dept = employee.getDepartment();
        return dept.getHead() != null && dept.getHead().getId().equals(employee.getId());
    }

    /**
     * Check if an employee is the department head of a specific department
     */
    public boolean isDepartmentHeadOf(Employee employee, Department department) {
        if (employee == null || department == null) {
            return false;
        }

        return department.getHead() != null &&
               department.getHead().getId().equals(employee.getId());
    }

    /**
     * Check if an employee is a parent department head
     */
    public boolean isParentDepartmentHeadOf(Employee employee, Department department) {
        if (employee == null || department == null) {
            return false;
        }

        Department parent = department.getParent();
        while (parent != null) {
            if (parent.getHead() != null &&
                parent.getHead().getId().equals(employee.getId())) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }

    /**
     * Check if an employee has HR role
     */
    public boolean isHR(Employee employee) {
        if (employee == null) {
            return false;
        }

        // Check employee roles (assuming roles are loaded)
        // This will need to be adjusted based on your role implementation
        return employeeRepository.hasRole(employee.getId(), "HR");
    }

    /**
     * Get the department head for a department
     */
    private Employee getDepartmentHead(Department department, Employee excludeEmployee) {
        if (department == null) {
            return null;
        }

        Employee head = department.getHead();
        if (head != null && (excludeEmployee == null || !head.getId().equals(excludeEmployee.getId()))) {
            return head;
        }

        return null;
    }

    /**
     * Get the parent department head
     * Traverses up the department hierarchy to find a suitable approver
     */
    private Employee getParentDepartmentHead(Department department, Employee excludeEmployee) {
        if (department == null) {
            return null;
        }

        Department parent = department.getParent();
        while (parent != null) {
            Employee parentHead = parent.getHead();
            if (parentHead != null &&
                (excludeEmployee == null || !parentHead.getId().equals(excludeEmployee.getId()))) {
                return parentHead;
            }
            parent = parent.getParent();
        }

        return null;
    }

    /**
     * Get an HR user as fallback approver
     */
    private Employee getHRUser() {
        // Find first active employee with HR role
        // This is a simple implementation - you may want to enhance this
        List<Employee> hrUsers = employeeRepository.findByRole("HR");
        if (hrUsers != null && !hrUsers.isEmpty()) {
            return hrUsers.get(0);
        }

        // Last resort: return admin user
        return employeeRepository.findByEmailAndDeletedAtIsNull("admin@hris.local")
                .orElseThrow(() -> new IllegalStateException("No HR or admin user available for approval"));
    }

    /**
     * Calculate the duration of a leave request in days
     */
    private long getLeaveDurationDays(LeaveRequest leaveRequest) {
        if (leaveRequest == null ||
            leaveRequest.getStartDate() == null ||
            leaveRequest.getEndDate() == null) {
            return 0;
        }

        return ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
    }

    /**
     * Set the current approver for a leave request
     */
    public void setCurrentApprover(LeaveRequest leaveRequest) {
        if (leaveRequest == null || leaveRequest.getEmployee() == null) {
            throw new IllegalArgumentException("Leave request and employee cannot be null");
        }

        Employee approver = getApprover(leaveRequest.getEmployee(), leaveRequest);
        leaveRequest.setCurrentApproverId(approver.getId());
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        log.info("Set current approver for leave request {} to: {} (ID: {})",
                leaveRequest.getId(), approver.getFullName(), approver.getId());
    }

    /**
     * Check if approval is complete for a leave request
     */
    public boolean isApprovalComplete(LeaveRequest leaveRequest) {
        if (leaveRequest == null) {
            return false;
        }

        // If approved or rejected, approval is complete
        if (leaveRequest.getStatus() == LeaveRequestStatus.APPROVED ||
            leaveRequest.getStatus() == LeaveRequestStatus.REJECTED) {
            return true;
        }

        // If still pending, check if there are more approvers in chain
        long durationDays = getLeaveDurationDays(leaveRequest);
        Employee requester = leaveRequest.getEmployee();

        // For short leave, single approval is enough
        if (durationDays <= 3) {
            return leaveRequest.getApprovedBy() != null;
        }

        // For long leave, need to check if HR approved
        // This is a simplified check - you may need more complex logic
        return leaveRequest.getApprovedBy() != null && isHR(leaveRequest.getApprovedBy());
    }
}
