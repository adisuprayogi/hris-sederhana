package com.hris.repository;

import com.hris.model.EmployeeShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Employee Shift Schedule Repository
 */
@Repository
public interface EmployeeShiftScheduleRepository extends JpaRepository<EmployeeShiftSchedule, Long> {

    /**
     * Find schedule for employee on specific date
     */
    Optional<EmployeeShiftSchedule> findByEmployeeIdAndScheduleDateAndDeletedAtIsNull(Long employeeId, LocalDate date);

    /**
     * Find all schedules for an employee in a date range
     */
    @Query("SELECT ess FROM EmployeeShiftSchedule ess WHERE ess.employeeId = :employeeId AND ess.scheduleDate >= :startDate AND ess.scheduleDate <= :endDate AND ess.deletedAt IS NULL ORDER BY ess.scheduleDate ASC")
    List<EmployeeShiftSchedule> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find all schedules for an employee
     */
    List<EmployeeShiftSchedule> findByEmployeeIdAndDeletedAtIsNullOrderByScheduleDateAsc(Long employeeId);

    /**
     * Find all schedules in a date range (for multiple employees)
     */
    @Query("SELECT ess FROM EmployeeShiftSchedule ess WHERE ess.scheduleDate >= :startDate AND ess.scheduleDate <= :endDate AND ess.deletedAt IS NULL ORDER BY ess.employeeId, ess.scheduleDate")
    List<EmployeeShiftSchedule> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find all schedules for today
     */
    @Query("SELECT ess FROM EmployeeShiftSchedule ess WHERE ess.scheduleDate = CURRENT_DATE AND ess.deletedAt IS NULL")
    List<EmployeeShiftSchedule> findTodaySchedules();

    /**
     * Find all schedules with working hours fetched
     */
    @Query("SELECT ess FROM EmployeeShiftSchedule ess LEFT JOIN FETCH ess.workingHours LEFT JOIN FETCH ess.employee WHERE ess.employeeId = :employeeId AND ess.scheduleDate >= :startDate AND ess.scheduleDate <= :endDate AND ess.deletedAt IS NULL ORDER BY ess.scheduleDate ASC")
    List<EmployeeShiftSchedule> findByEmployeeIdAndDateRangeWithWorkingHours(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Delete schedules for employee in date range (soft delete)
     */
    @Query("UPDATE EmployeeShiftSchedule ess SET ess.deletedAt = CURRENT_TIMESTAMP WHERE ess.employeeId = :employeeId AND ess.scheduleDate >= :startDate AND ess.scheduleDate <= :endDate AND ess.deletedAt IS NULL")
    int softDeleteByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
