package com.hris.repository;

import com.hris.model.Employee;
import com.hris.model.LeaveRequest;
import com.hris.model.enums.LeaveRequestStatus;
import com.hris.model.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository untuk LeaveRequest Entity
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Find all leave requests by employee
     */
    List<LeaveRequest> findByEmployeeIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long employeeId);

    /**
     * Find leave requests by status
     */
    List<LeaveRequest> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(LeaveRequestStatus status);

    /**
     * Find pending leave requests for approver
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.currentApprover.id = :approverId " +
            "AND lr.status = 'PENDING' AND lr.deletedAt IS NULL " +
            "ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findPendingRequestsForApprover(@Param("approverId") Long approverId);

    /**
     * Find leave requests by type
     */
    List<LeaveRequest> findByLeaveTypeAndDeletedAtIsNullOrderByCreatedAtDesc(LeaveType leaveType);

    /**
     * Find leave requests in date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "((lr.startDate >= :startDate AND lr.startDate <= :endDate) OR " +
            "(lr.endDate >= :startDate AND lr.endDate <= :endDate) OR " +
            "(lr.startDate <= :startDate AND lr.endDate >= :endDate)) " +
            "AND lr.status IN ('PENDING', 'APPROVED') " +
            "AND lr.deletedAt IS NULL " +
            "ORDER BY lr.startDate DESC")
    List<LeaveRequest> findLeaveRequestsInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find overlapping leave requests for employee
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
            "AND lr.status IN ('PENDING', 'APPROVED') " +
            "AND lr.deletedAt IS NULL " +
            "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaveRequests(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find approved leave requests for employee in date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
            "AND lr.status = 'APPROVED' " +
            "AND ((lr.startDate >= :startDate AND lr.startDate <= :endDate) OR " +
            "(lr.endDate >= :startDate AND lr.endDate <= :endDate) OR " +
            "(lr.startDate <= :startDate AND lr.endDate >= :endDate)) " +
            "AND lr.deletedAt IS NULL")
    List<LeaveRequest> findApprovedLeaveInDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count leave requests by status
     */
    long countByStatusAndDeletedAtIsNull(LeaveRequestStatus status);

    /**
     * Count pending requests for approver
     */
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.currentApprover.id = :approverId " +
            "AND lr.status = 'PENDING' AND lr.deletedAt IS NULL")
    long countPendingRequestsForApprover(@Param("approverId") Long approverId);

    /**
     * Get leave statistics for employee in a year
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
            "AND YEAR(lr.startDate) = :year " +
            "AND lr.status = 'APPROVED' " +
            "AND lr.deletedAt IS NULL")
    List<LeaveRequest> findApprovedLeaveInYear(
            @Param("employeeId") Long employeeId,
            @Param("year") int year);
}
