package com.hris.repository;

import com.hris.model.TeachingAttendanceStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TeachingAttendanceStagingRepository extends JpaRepository<TeachingAttendanceStaging, Long> {

    List<TeachingAttendanceStaging> findAllByDeletedAtIsNull();

    List<TeachingAttendanceStaging> findByLecturerIdAndDeletedAtIsNull(Long lecturerId);

    List<TeachingAttendanceStaging> findByAttendanceDateBetweenAndDeletedAtIsNull(
            LocalDate startDate, LocalDate endDate);

    List<TeachingAttendanceStaging> findByLecturerIdAndAttendanceDateBetweenAndDeletedAtIsNull(
            Long lecturerId, LocalDate startDate, LocalDate endDate);

    List<TeachingAttendanceStaging> findByPayrollPeriodUsedIsNullAndDeletedAtIsNull();

    @Query("SELECT t FROM TeachingAttendanceStaging t WHERE t.lecturerId = :lecturerId " +
            "AND t.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND t.attendanceStatus = 'HADIR' AND t.payrollPeriodUsed IS NULL " +
            "AND t.deletedAt IS NULL")
    List<TeachingAttendanceStaging> findPresentAttendanceByLecturerAndDateRange(
            @Param("lecturerId") Long lecturerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM TeachingAttendanceStaging t WHERE t.lecturerId = :lecturerId " +
            "AND t.payrollPeriodUsed IS NULL AND t.deletedAt IS NULL")
    List<TeachingAttendanceStaging> findUnprocessedByLecturerId(@Param("lecturerId") Long lecturerId);
}
