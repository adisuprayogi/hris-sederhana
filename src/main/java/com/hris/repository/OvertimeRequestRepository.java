package com.hris.repository;

import com.hris.model.OvertimeRequest;
import com.hris.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Overtime Request Repository
 */
@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {

    /**
     * Find overtime request by employee and date
     */
    Optional<OvertimeRequest> findByEmployeeIdAndRequestDateAndDeletedAtIsNull(
            Long employeeId, LocalDate requestDate);

    /**
     * Find approved overtime request by employee and date
     */
    Optional<OvertimeRequest> findByEmployeeIdAndRequestDateAndStatusAndDeletedAtIsNull(
            Long employeeId, LocalDate requestDate, RequestStatus status);

    /**
     * Find overtime requests by employee
     */
    List<OvertimeRequest> findByEmployeeIdAndDeletedAtIsNullOrderByRequestDateDesc(
            Long employeeId);

    /**
     * Find overtime requests by employee and date range
     */
    List<OvertimeRequest> findByEmployeeIdAndRequestDateBetweenAndDeletedAtIsNullOrderByRequestDateDesc(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Find pending supervisor requests
     */
    @Query("SELECT otr FROM OvertimeRequest otr " +
            "WHERE otr.status = 'PENDING_SUPERVISOR' " +
            "AND otr.deletedAt IS NULL " +
            "ORDER BY otr.createdAt ASC")
    List<OvertimeRequest> findPendingSupervisorRequests();

    /**
     * Find pending HR requests
     */
    @Query("SELECT otr FROM OvertimeRequest otr " +
            "WHERE otr.status = 'PENDING_HR' " +
            "AND otr.deletedAt IS NULL " +
            "ORDER BY otr.createdAt ASC")
    List<OvertimeRequest> findPendingHrRequests();

    /**
     * Find overtime requests by date range
     */
    List<OvertimeRequest> findByRequestDateBetweenAndDeletedAtIsNullOrderByRequestDateAsc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find approved overtime requests by date range
     */
    List<OvertimeRequest> findByStatusAndRequestDateBetweenAndDeletedAtIsNullOrderByRequestDateAsc(
            RequestStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * Check if employee has approved overtime for specific date
     */
    @Query("SELECT CASE WHEN COUNT(otr) > 0 THEN true ELSE false END FROM OvertimeRequest otr " +
            "WHERE otr.employeeId = :employeeId " +
            "AND otr.requestDate = :date " +
            "AND otr.status = 'APPROVED' " +
            "AND otr.deletedAt IS NULL")
    boolean hasApprovedOvertimeForDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    /**
     * Get total overtime minutes for employee in date range
     */
    @Query("SELECT COALESCE(SUM(otr.actualDurationMinutes), 0) FROM OvertimeRequest otr " +
            "WHERE otr.employeeId = :employeeId " +
            "AND otr.requestDate BETWEEN :startDate AND :endDate " +
            "AND otr.status = 'APPROVED' " +
            "AND otr.deletedAt IS NULL")
    Integer getTotalOvertimeMinutesByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    /**
     * Count overtime days for employee in date range
     */
    @Query("SELECT COUNT(otr) FROM OvertimeRequest otr " +
            "WHERE otr.employeeId = :employeeId " +
            "AND otr.requestDate BETWEEN :startDate AND :endDate " +
            "AND otr.status = 'APPROVED' " +
            "AND otr.deletedAt IS NULL")
    long countApprovedOvertimeDaysByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);
}
