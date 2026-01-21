package com.hris.model;

import com.hris.model.enums.LeaveRequestStatus;
import com.hris.model.enums.LeaveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Leave Request Entity
 * Represents an employee's leave/cuti request with 2-level approval workflow
 *
 * Approval Flow:
 * 1. PENDING_SUPERVISOR → Waiting for supervisor approval
 * 2. PENDING_HR → Waiting for HR/Admin approval (supervisor approved)
 * 3. APPROVED → Fully approved
 * 4. REJECTED_BY_SUPERVISOR → Rejected by supervisor
 * 5. REJECTED_BY_HR → Rejected by HR
 */
@Entity
@Table(name = "leave_requests")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "employee_id", insertable = false, updatable = false)
    private Long employeeId;

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
    // 2-LEVEL APPROVAL FIELDS
    // =====================================================

    /**
     * Current status of the leave request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    @Builder.Default
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING_SUPERVISOR;

    // ---------- Level 1: Supervisor Approval ----------

    /**
     * Supervisor (atasan langsung) who approved/rejected
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    @Column(name = "supervisor_id", insertable = false, updatable = false)
    private Long supervisorId;

    /**
     * Timestamp when supervisor approved/rejected
     */
    @Column(name = "supervisor_approved_at")
    private LocalDateTime supervisorApprovedAt;

    /**
     * Supervisor's approval/rejection note
     */
    @Column(name = "supervisor_approval_note", columnDefinition = "TEXT")
    private String supervisorApprovalNote;

    // ---------- Level 2: HR/Admin Approval ----------

    /**
     * HR/Admin who approved/rejected
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_id")
    private Employee hr;

    @Column(name = "hr_id", insertable = false, updatable = false)
    private Long hrId;

    /**
     * Timestamp when HR approved/rejected
     */
    @Column(name = "hr_approved_at")
    private LocalDateTime hrApprovedAt;

    /**
     * HR's approval/rejection note
     */
    @Column(name = "hr_approval_note", columnDefinition = "TEXT")
    private String hrApprovalNote;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Get the number of days for this leave request
     */
    public long getDurationDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Check if this is a short leave (3 days or less)
     */
    public boolean isShortLeave() {
        return getDurationDays() <= 3;
    }

    /**
     * Check if this leave request is pending approval
     */
    public boolean isPending() {
        return this.status != null && this.status.isPending();
    }

    /**
     * Check if this leave request has been approved
     */
    public boolean isApproved() {
        return this.status != null && this.status.isApproved();
    }

    /**
     * Check if this leave request has been rejected
     */
    public boolean isRejected() {
        return this.status != null && this.status.isRejected();
    }

    /**
     * Check if currently waiting for supervisor approval
     */
    public boolean isPendingSupervisor() {
        return this.status == LeaveRequestStatus.PENDING_SUPERVISOR;
    }

    /**
     * Check if currently waiting for HR approval
     */
    public boolean isPendingHr() {
        return this.status == LeaveRequestStatus.PENDING_HR;
    }

    /**
     * Approve at supervisor level
     */
    public void approveBySupervisor(Employee supervisor, String note) {
        this.status = LeaveRequestStatus.PENDING_HR;
        this.supervisor = supervisor;
        this.supervisorId = supervisor != null ? supervisor.getId() : null;
        this.supervisorApprovedAt = LocalDateTime.now();
        this.supervisorApprovalNote = note;
    }

    /**
     * Reject at supervisor level
     */
    public void rejectBySupervisor(Employee supervisor, String reason) {
        this.status = LeaveRequestStatus.REJECTED_BY_SUPERVISOR;
        this.supervisor = supervisor;
        this.supervisorId = supervisor != null ? supervisor.getId() : null;
        this.supervisorApprovedAt = LocalDateTime.now();
        this.supervisorApprovalNote = reason;
    }

    /**
     * Approve at HR level (final approval)
     */
    public void approveByHr(Employee hr, String note) {
        this.status = LeaveRequestStatus.APPROVED;
        this.hr = hr;
        this.hrId = hr != null ? hr.getId() : null;
        this.hrApprovedAt = LocalDateTime.now();
        this.hrApprovalNote = note;
    }

    /**
     * Reject at HR level
     */
    public void rejectByHr(Employee hr, String reason) {
        this.status = LeaveRequestStatus.REJECTED_BY_HR;
        this.hr = hr;
        this.hrId = hr != null ? hr.getId() : null;
        this.hrApprovedAt = LocalDateTime.now();
        this.hrApprovalNote = reason;
    }
}
