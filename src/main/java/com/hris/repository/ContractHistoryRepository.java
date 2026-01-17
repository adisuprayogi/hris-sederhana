package com.hris.repository;

import com.hris.model.ContractHistory;
import com.hris.model.enums.EmploymentStatus;
import com.hris.model.enums.EmploymentStatusChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk ContractHistory
 */
@Repository
public interface ContractHistoryRepository extends JpaRepository<ContractHistory, Long> {

    /**
     * Find all contract history for an employee (non-deleted, ordered by start date desc)
     */
    List<ContractHistory> findByEmployeeIdAndDeletedAtIsNullOrderByStartDateDesc(Long employeeId);

    /**
     * Find current employment status for an employee (latest record with no end date)
     */
    Optional<ContractHistory> findByEmployeeIdAndEndDateIsNullAndDeletedAtIsNullOrderByStartDateDesc(Long employeeId);

    /**
     * Find all contract periods for an employee
     */
    List<ContractHistory> findByEmployeeIdAndNewStatusAndDeletedAtIsNullOrderByStartDateDesc(
        Long employeeId, EmploymentStatus status
    );

    /**
     * Find permanent appointment history for an employee
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.employee.id = :employeeId " +
           "AND ch.newStatus = 'PERMANENT' AND ch.deletedAt IS NULL " +
           "ORDER BY ch.startDate DESC")
    List<ContractHistory> findPermanentAppointmentHistory(@Param("employeeId") Long employeeId);

    /**
     * Find contracts expiring within date range
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.newStatus = 'CONTRACT' " +
           "AND ch.endDate BETWEEN :startDate AND :endDate AND ch.deletedAt IS NULL " +
           "ORDER BY ch.endDate ASC")
    List<ContractHistory> findExpiringContracts(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Find contracts expiring soon (within next N days)
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.newStatus = 'CONTRACT' " +
           "AND ch.endDate BETWEEN :today AND :expiryDate AND ch.deletedAt IS NULL " +
           "ORDER BY ch.endDate ASC")
    List<ContractHistory> findContractsExpiringSoon(@Param("today") LocalDate today,
                                                      @Param("expiryDate") LocalDate expiryDate);

    /**
     * Find all change history by type
     */
    List<ContractHistory> findByChangeTypeAndDeletedAtIsNullOrderByStartDateDesc(
        EmploymentStatusChange changeType
    );

    /**
     * Check if employee has active contract
     */
    @Query("SELECT COUNT(ch) > 0 FROM ContractHistory ch WHERE ch.employee.id = :employeeId " +
           "AND ch.newStatus = 'CONTRACT' AND ch.endDate IS NULL AND ch.deletedAt IS NULL")
    boolean hasActiveContract(@Param("employeeId") Long employeeId);

    /**
     * Find latest contract renewal for an employee
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.employee.id = :employeeId " +
           "AND ch.changeType = 'CONTRACT_RENEWAL' AND ch.deletedAt IS NULL " +
           "ORDER BY ch.startDate DESC")
    List<ContractHistory> findLatestContractRenewal(@Param("employeeId") Long employeeId);

    /**
     * Get all active contracts (end_date is null or in the future)
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.newStatus = 'CONTRACT' " +
           "AND (ch.endDate IS NULL OR ch.endDate >= :today) AND ch.deletedAt IS NULL " +
           "ORDER BY ch.endDate ASC")
    List<ContractHistory> findAllActiveContracts(@Param("today") LocalDate today);

    /**
     * Get employment history count for an employee
     */
    @Query("SELECT COUNT(ch) FROM ContractHistory ch WHERE ch.employee.id = :employeeId " +
           "AND ch.deletedAt IS NULL")
    long countByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find first employment record (hiring) for an employee
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.employee.id = :employeeId " +
           "AND ch.changeType = 'INITIAL_HIRING' AND ch.deletedAt IS NULL " +
           "ORDER BY ch.startDate ASC")
    List<ContractHistory> findInitialHiring(@Param("employeeId") Long employeeId);
}
