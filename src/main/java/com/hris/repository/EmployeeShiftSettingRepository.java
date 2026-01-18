package com.hris.repository;

import com.hris.model.EmployeeShiftSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Employee Shift Setting Repository
 */
@Repository
public interface EmployeeShiftSettingRepository extends JpaRepository<EmployeeShiftSetting, Long> {

    /**
     * Find all settings for an employee
     */
    List<EmployeeShiftSetting> findByEmployeeIdAndDeletedAtIsNullOrderByEffectiveFromDesc(Long employeeId);

    /**
     * Find active setting for an employee on a specific date
     */
    @Query("SELECT ess FROM EmployeeShiftSetting ess WHERE ess.employeeId = :employeeId AND ess.effectiveFrom <= :date AND (ess.effectiveTo IS NULL OR ess.effectiveTo >= :date) AND ess.deletedAt IS NULL ORDER BY ess.effectiveFrom DESC")
    Optional<EmployeeShiftSetting> findActiveByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    /**
     * Find currently active setting for an employee
     */
    @Query("SELECT ess FROM EmployeeShiftSetting ess WHERE ess.employeeId = :employeeId AND ess.effectiveFrom <= CURRENT_DATE AND (ess.effectiveTo IS NULL OR ess.effectiveTo >= CURRENT_DATE) AND ess.deletedAt IS NULL ORDER BY ess.effectiveFrom DESC")
    Optional<EmployeeShiftSetting> findCurrentActiveByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find settings with shift pattern fetched
     */
    @Query("SELECT ess FROM EmployeeShiftSetting ess LEFT JOIN FETCH ess.shiftPattern WHERE ess.employeeId = :employeeId AND ess.deletedAt IS NULL ORDER BY ess.effectiveFrom DESC")
    List<EmployeeShiftSetting> findByEmployeeIdWithShiftPattern(@Param("employeeId") Long employeeId);

    /**
     * Find previous setting (before a date)
     */
    @Query("SELECT ess FROM EmployeeShiftSetting ess WHERE ess.employeeId = :employeeId AND ess.effectiveFrom < :date AND ess.deletedAt IS NULL ORDER BY ess.effectiveFrom DESC LIMIT 1")
    Optional<EmployeeShiftSetting> findPreviousSetting(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    /**
     * Find all employees that need to be closed on a specific date
     * (for auto-closing when assigning new pattern)
     */
    @Query("SELECT ess FROM EmployeeShiftSetting ess WHERE ess.employeeId = :employeeId AND ess.effectiveTo IS NULL AND ess.effectiveFrom < :date AND ess.deletedAt IS NULL")
    Optional<EmployeeShiftSetting> findOpenSettingBeforeDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}
