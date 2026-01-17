package com.hris.repository;

import com.hris.model.SalaryHistory;
import com.hris.model.enums.SalaryChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for SalaryHistory entity
 */
@Repository
public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, Long> {

    /**
     * Find all salary history for an employee
     */
    @Query("SELECT sh FROM SalaryHistory sh " +
           "WHERE sh.employee.id = :employeeId " +
           "ORDER BY sh.effectiveDate DESC")
    List<SalaryHistory> findByEmployeeIdOrderByEffectiveDateDesc(@Param("employeeId") Long employeeId);

    /**
     * Find salary history by change type
     */
    List<SalaryHistory> findByEmployeeIdAndChangeType(
            @Param("employeeId") Long employeeId,
            @Param("changeType") SalaryChangeType changeType
    );

    /**
     * Find salary history within date range
     */
    @Query("SELECT sh FROM SalaryHistory sh " +
           "WHERE sh.employee.id = :employeeId " +
           "AND sh.effectiveDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sh.effectiveDate DESC")
    List<SalaryHistory> findByEmployeeIdAndEffectiveDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find latest salary for an employee
     */
    @Query("SELECT sh FROM SalaryHistory sh " +
           "WHERE sh.employee.id = :employeeId " +
           "ORDER BY sh.effectiveDate DESC " +
           "LIMIT 1")
    SalaryHistory findLatestByEmployeeId(@Param("employeeId") Long employeeId);
}
