package com.hris.repository;

import com.hris.model.AttendanceRecord;
import com.hris.model.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Attendance Record Repository
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * Find attendance record by employee and date
     */
    @Query("SELECT a FROM AttendanceRecord a " +
            "LEFT JOIN FETCH a.shiftPattern sp " +
            "LEFT JOIN FETCH a.workingHours wh " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.attendanceDate = :attendanceDate " +
            "AND a.deletedAt IS NULL")
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDateAndDeletedAtIsNull(
            @Param("employeeId") Long employeeId,
            @Param("attendanceDate") LocalDate attendanceDate);

    /**
     * Find attendance records by employee and date range (with eager fetch)
     */
    @Query("SELECT a FROM AttendanceRecord a " +
            "LEFT JOIN FETCH a.shiftPattern sp " +
            "LEFT JOIN FETCH a.workingHours wh " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND a.deletedAt IS NULL " +
            "ORDER BY a.attendanceDate DESC")
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateBetweenAndDeletedAtIsNullOrderByAttendanceDateDesc(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find all attendance records for a specific date
     */
    List<AttendanceRecord> findByAttendanceDateAndDeletedAtIsNull(LocalDate attendanceDate);

    /**
     * Find attendance records by date range
     */
    List<AttendanceRecord> findByAttendanceDateBetweenAndDeletedAtIsNullOrderByAttendanceDateAsc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find late records by date range
     */
    List<AttendanceRecord> findByIsLateTrueAndAttendanceDateBetweenAndDeletedAtIsNull(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find overtime records by date range
     */
    List<AttendanceRecord> findByIsOvertimeTrueAndAttendanceDateBetweenAndDeletedAtIsNull(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find WFH records by date range
     */
    List<AttendanceRecord> findByIsWfhTrueAndAttendanceDateBetweenAndDeletedAtIsNull(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find attendance records by status
     */
    List<AttendanceRecord> findByStatusAndAttendanceDateBetweenAndDeletedAtIsNull(
            AttendanceStatus status, LocalDate startDate, LocalDate endDate);

    /**
     * Get monthly summary for employee
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND a.deletedAt IS NULL")
    long countByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * Count late days for employee in date range
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND a.isLate = true " +
            "AND a.deletedAt IS NULL")
    long countLateDaysByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Count overtime days for employee in date range
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND a.isOvertime = true " +
            "AND a.deletedAt IS NULL")
    long countOvertimeDaysByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Check if employee has already clocked in today
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AttendanceRecord a " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.attendanceDate = :date " +
            "AND a.clockInTime IS NOT NULL " +
            "AND a.deletedAt IS NULL")
    boolean hasClockedInToday(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    /**
     * Check if employee has already clocked out today
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AttendanceRecord a " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.attendanceDate = :date " +
            "AND a.clockOutTime IS NOT NULL " +
            "AND a.deletedAt IS NULL")
    boolean hasClockedOutToday(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}
