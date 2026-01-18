package com.hris.repository;

import com.hris.model.TeachingScheduleStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeachingScheduleStagingRepository extends JpaRepository<TeachingScheduleStaging, Long> {

    List<TeachingScheduleStaging> findAllByDeletedAtIsNull();

    List<TeachingScheduleStaging> findByLecturerIdAndDeletedAtIsNull(Long lecturerId);

    List<TeachingScheduleStaging> findByAcademicYearAndSemesterAndDeletedAtIsNull(
            String academicYear, String semester);

    List<TeachingScheduleStaging> findByLecturerIdAndAcademicYearAndSemesterAndDeletedAtIsNull(
            Long lecturerId, String academicYear, String semester);

    List<TeachingScheduleStaging> findByPayrollPeriodUsedIsNullAndDeletedAtIsNull();

    @Query("SELECT t FROM TeachingScheduleStaging t WHERE t.lecturerId = :lecturerId " +
            "AND t.payrollPeriodUsed IS NULL AND t.deletedAt IS NULL")
    List<TeachingScheduleStaging> findUnprocessedByLecturerId(@Param("lecturerId") Long lecturerId);

    @Query("SELECT DISTINCT t.academicYear FROM TeachingScheduleStaging t WHERE t.deletedAt IS NULL ORDER BY t.academicYear DESC")
    List<String> findDistinctAcademicYears();

    @Query("SELECT DISTINCT t.semester FROM TeachingScheduleStaging t WHERE t.academicYear = :year AND t.deletedAt IS NULL")
    List<String> findDistinctSemestersByYear(@Param("year") String year);
}
