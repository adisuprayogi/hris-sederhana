package com.hris.model;

import com.hris.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Overtime Request Entity
 * Represents an employee's overtime request with 2-level approval workflow
 *
 * Approval Flow:
 * 1. PENDING_SUPERVISOR → Waiting for supervisor approval
 * 2. PENDING_HR → Waiting for HR/Admin approval (supervisor approved)
 * 3. APPROVED → Fully approved (employee can do overtime)
 * 4. REJECTED_BY_SUPERVISOR → Rejected by supervisor
 * 5. REJECTED_BY_HR → Rejected by HR
 */
@Entity
@Table(name = "overtime_requests")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // EMPLOYEE & REQUEST DETAILS
    // =====================================================

    /**
     * Employee requesting overtime
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "employee_id", insertable = false, updatable = false)
    private Long employeeId;

    /**
     * Date for overtime
     */
    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    /**
     * Estimated overtime hours
     */
    @Column(name = "estimated_hours", precision = 4, scale = 2)
    private BigDecimal estimatedHours;

    /**
     * Reason for overtime request
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // =====================================================
    // 2-LEVEL APPROVAL FIELDS
    // =====================================================

    /**
     * Current status of the overtime request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING_SUPERVISOR;

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
    // ACTUAL OVERTIME DATA
    // =====================================================

    /**
     * Actual overtime duration in minutes
     * Filled after employee clocks out
     */
    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    public boolean isPending() {
        return this.status != null && this.status.isPending();
    }

    public boolean isApproved() {
        return this.status != null && this.status.isApproved();
    }

    public boolean isRejected() {
        return this.status != null && this.status.isRejected();
    }

    public boolean isPendingSupervisor() {
        return this.status == RequestStatus.PENDING_SUPERVISOR;
    }

    public boolean isPendingHr() {
        return this.status == RequestStatus.PENDING_HR;
    }

    /**
     * Approve at supervisor level
     */
    public void approveBySupervisor(Employee supervisor, String note) {
        this.status = RequestStatus.PENDING_HR;
        this.supervisor = supervisor;
        this.supervisorId = supervisor != null ? supervisor.getId() : null;
        this.supervisorApprovedAt = LocalDateTime.now();
        this.supervisorApprovalNote = note;
    }

    /**
     * Reject at supervisor level
     */
    public void rejectBySupervisor(Employee supervisor, String reason) {
        this.status = RequestStatus.REJECTED_BY_SUPERVISOR;
        this.supervisor = supervisor;
        this.supervisorId = supervisor != null ? supervisor.getId() : null;
        this.supervisorApprovedAt = LocalDateTime.now();
        this.supervisorApprovalNote = reason;
    }

    /**
     * Approve at HR level (final approval)
     */
    public void approveByHr(Employee hr, String note) {
        this.status = RequestStatus.APPROVED;
        this.hr = hr;
        this.hrId = hr != null ? hr.getId() : null;
        this.hrApprovedAt = LocalDateTime.now();
        this.hrApprovalNote = note;
    }

    /**
     * Reject at HR level
     */
    public void rejectByHr(Employee hr, String reason) {
        this.status = RequestStatus.REJECTED_BY_HR;
        this.hr = hr;
        this.hrId = hr != null ? hr.getId() : null;
        this.hrApprovedAt = LocalDateTime.now();
        this.hrApprovalNote = reason;
    }

    /**
     * Set actual overtime duration after clock out
     */
    public void setActualDuration(Integer minutes) {
        this.actualDurationMinutes = minutes;
    }
}
