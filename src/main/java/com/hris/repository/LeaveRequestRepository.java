package com.hris.repository;

import com.hris.model.LeaveRequest;
import com.hris.model.enums.LeaveRequestStatus;
import com.hris.model.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository untuk LeaveRequest Entity
 * Supports 2-level approval workflow
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
     * Find pending supervisor requests
     */
    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.status = 'PENDING_SUPERVISOR' " +
            "AND lr.deletedAt IS NULL " +
            "ORDER BY lr.createdAt ASC")
    List<LeaveRequest> findPendingSupervisorRequests();

    /**
     * Find pending HR requests
     */
    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.status = 'PENDING_HR' " +
            "AND lr.deletedAt IS NULL " +
            "ORDER BY lr.createdAt ASC")
    List<LeaveRequest> findPendingHrRequests();

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
            "AND lr.status IN ('PENDING_SUPERVISOR', 'PENDING_HR', 'APPROVED') " +
            "AND lr.deletedAt IS NULL " +
            "ORDER BY lr.startDate DESC")
    List<LeaveRequest> findLeaveRequestsInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find overlapping leave requests for employee
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
            "AND lr.status IN ('PENDING_SUPERVISOR', 'PENDING_HR', 'APPROVED') " +
            "AND lr.deletedAt IS NULL " +
            "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaveRequests(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find approved leave requests for employee in date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
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
     * Get leave statistics for employee in a year
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
            "AND YEAR(lr.startDate) = :year " +
            "AND lr.status = 'APPROVED' " +
            "AND lr.deletedAt IS NULL")
    List<LeaveRequest> findApprovedLeaveInYear(
            @Param("employeeId") Long employeeId,
            @Param("year") int year);

    /**
     * Check if employee has approved leave on specific date
     */
    @Query("SELECT CASE WHEN COUNT(lr) > 0 THEN true ELSE false END FROM LeaveRequest lr " +
            "WHERE lr.employeeId = :employeeId " +
            "AND :date BETWEEN lr.startDate AND lr.endDate " +
            "AND lr.status = 'APPROVED' " +
            "AND lr.deletedAt IS NULL")
    boolean hasApprovedLeaveOnDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    /**
     * Find leave requests by supervisor
     */
    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.supervisorId = :supervisorId " +
            "AND lr.status IN ('PENDING_SUPERVISOR', 'PENDING_HR') " +
            "AND lr.deletedAt IS NULL " +
            "ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findBySupervisorIdOrderByCreatedAtDesc(@Param("supervisorId") Long supervisorId);
}
