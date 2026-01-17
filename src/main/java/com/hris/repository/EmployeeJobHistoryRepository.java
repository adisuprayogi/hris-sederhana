package com.hris.repository;

import com.hris.model.EmployeeJobHistory;
import com.hris.model.enums.ChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for EmployeeJobHistory entity
 */
@Repository
public interface EmployeeJobHistoryRepository extends JpaRepository<EmployeeJobHistory, Long> {

    /**
     * Find all job history for an employee
     */
    @Query("SELECT jh FROM EmployeeJobHistory jh " +
           "LEFT JOIN FETCH jh.department " +
           "LEFT JOIN FETCH jh.position " +
           "WHERE jh.employee.id = :employeeId " +
           "ORDER BY jh.effectiveDate DESC")
    List<EmployeeJobHistory> findByEmployeeIdOrderByEffectiveDateDesc(@Param("employeeId") Long employeeId);

    /**
     * Find current job for an employee
     */
    @Query("SELECT jh FROM EmployeeJobHistory jh " +
           "LEFT JOIN FETCH jh.department " +
           "LEFT JOIN FETCH jh.position " +
           "WHERE jh.employee.id = :employeeId AND jh.isCurrent = true")
    EmployeeJobHistory findCurrentByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find job history by change type
     */
    List<EmployeeJobHistory> findByEmployeeIdAndChangeType(
            @Param("employeeId") Long employeeId,
            @Param("changeType") ChangeType changeType
    );

    /**
     * Find job history within date range
     */
    @Query("SELECT jh FROM EmployeeJobHistory jh " +
           "WHERE jh.employee.id = :employeeId " +
           "AND jh.effectiveDate BETWEEN :startDate AND :endDate " +
           "ORDER BY jh.effectiveDate DESC")
    List<EmployeeJobHistory> findByEmployeeIdAndEffectiveDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Check if employee has active job history
     */
    boolean existsByEmployeeIdAndIsCurrentTrue(@Param("employeeId") Long employeeId);
}
