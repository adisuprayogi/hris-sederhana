package com.hris.repository;

import com.hris.model.WfhRequest;
import com.hris.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * WFH Request Repository
 */
@Repository
public interface WfhRequestRepository extends JpaRepository<WfhRequest, Long> {

    /**
     * Find WFH request by employee and date
     */
    Optional<WfhRequest> findByEmployeeIdAndRequestDateAndDeletedAtIsNull(
            Long employeeId, LocalDate requestDate);

    /**
     * Find approved WFH request by employee and date
     */
    Optional<WfhRequest> findByEmployeeIdAndRequestDateAndStatusAndDeletedAtIsNull(
            Long employeeId, LocalDate requestDate, RequestStatus status);

    /**
     * Find WFH requests by employee
     */
    List<WfhRequest> findByEmployeeIdAndDeletedAtIsNullOrderByRequestDateDesc(
            Long employeeId);

    /**
     * Find WFH requests by employee and date range
     */
    List<WfhRequest> findByEmployeeIdAndRequestDateBetweenAndDeletedAtIsNullOrderByRequestDateDesc(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Find pending supervisor requests
     */
    List<WfhRequest> findByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(
            RequestStatus status);

    /**
     * Find pending requests for specific supervisor
     */
    @Query("SELECT wr FROM WfhRequest wr " +
            "WHERE wr.status = 'PENDING_SUPERVISOR' " +
            "AND wr.deletedAt IS NULL " +
            "ORDER BY wr.createdAt ASC")
    List<WfhRequest> findPendingSupervisorRequests();

    /**
     * Find pending HR requests
     */
    @Query("SELECT wr FROM WfhRequest wr " +
            "WHERE wr.status = 'PENDING_HR' " +
            "AND wr.deletedAt IS NULL " +
            "ORDER BY wr.createdAt ASC")
    List<WfhRequest> findPendingHrRequests();

    /**
     * Find WFH requests by date range
     */
    List<WfhRequest> findByRequestDateBetweenAndDeletedAtIsNullOrderByRequestDateAsc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Check if employee has approved WFH for specific date
     */
    @Query("SELECT CASE WHEN COUNT(wr) > 0 THEN true ELSE false END FROM WfhRequest wr " +
            "WHERE wr.employeeId = :employeeId " +
            "AND wr.requestDate = :date " +
            "AND wr.status = 'APPROVED' " +
            "AND wr.deletedAt IS NULL")
    boolean hasApprovedWfhForDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    /**
     * Count WFH days for employee in date range
     */
    @Query("SELECT COUNT(wr) FROM WfhRequest wr " +
            "WHERE wr.employeeId = :employeeId " +
            "AND wr.requestDate BETWEEN :startDate AND :endDate " +
            "AND wr.status = 'APPROVED' " +
            "AND wr.deletedAt IS NULL")
    long countApprovedWfhDaysByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}
