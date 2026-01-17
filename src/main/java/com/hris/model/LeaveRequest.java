package com.hris.model;

import com.hris.model.enums.LeaveRequestStatus;
import com.hris.model.enums.LeaveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Leave Request Entity
 * Represents an employee's leave/cuti request with approval workflow
 */
@Entity
@Table(name = "leave_requests")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // EMPLOYEE & LEAVE DETAILS
    // =====================================================

    /**
     * Employee requesting the leave
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Type of leave (ANNUAL, SICK, MATERNITY, MARRIAGE, SPECIAL, UNPAID)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    /**
     * Start date of the leave
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of the leave
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Reason for the leave request
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // =====================================================
    // APPROVAL FIELDS
    // =====================================================

    /**
     * Current status of the leave request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;

    /**
     * Current approver in the approval chain
     * This represents who is currently responsible for approving this request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_approver_id")
    private Employee currentApprover;

    /**
     * Column for storing current approver ID directly
     * Used for queries and when the employee entity is not loaded
     * Read-only field - the actual relationship is managed by currentApprover
     */
    @Column(name = "current_approver_id", insertable = false, updatable = false)
    private Long currentApproverId;

    /**
     * The final approver who approved or rejected this request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    /**
     * Timestamp when the request was approved or rejected
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Rejection reason (if rejected)
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Get the number of days for this leave request
     *
     * @return Number of leave days
     */
    public long getDurationDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Check if this is a short leave (3 days or less)
     *
     * @return true if leave duration is 3 days or less
     */
    public boolean isShortLeave() {
        return getDurationDays() <= 3;
    }

    /**
     * Check if this leave request is pending approval
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return this.status == LeaveRequestStatus.PENDING;
    }

    /**
     * Check if this leave request has been approved
     *
     * @return true if status is APPROVED
     */
    public boolean isApproved() {
        return this.status == LeaveRequestStatus.APPROVED;
    }

    /**
     * Check if this leave request has been rejected
     *
     * @return true if status is REJECTED
     */
    public boolean isRejected() {
        return this.status == LeaveRequestStatus.REJECTED;
    }

    /**
     * Approve this leave request
     *
     * @param approver The employee who approved this request
     */
    public void approve(Employee approver) {
        this.status = LeaveRequestStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Reject this leave request
     *
     * @param approver The employee who rejected this request
     * @param reason   The reason for rejection
     */
    public void reject(Employee approver, String reason) {
        this.status = LeaveRequestStatus.REJECTED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * Set the current approver for this request
     *
     * @param approver The current approver
     */
    public void setCurrentApprover(Employee approver) {
        this.currentApprover = approver;
        this.currentApproverId = approver != null ? approver.getId() : null;
    }

    /**
     * Check if the specified employee is the current approver
     *
     * @param employee The employee to check
     * @return true if this employee is the current approver
     */
    public boolean isCurrentApprover(Employee employee) {
        if (employee == null) {
            return false;
        }
        return employee.getId().equals(this.currentApproverId);
    }
}
