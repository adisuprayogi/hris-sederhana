package com.hris.repository;

import com.hris.model.ThesisExaminationStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ThesisExaminationStagingRepository extends JpaRepository<ThesisExaminationStaging, Long> {

    List<ThesisExaminationStaging> findAllByDeletedAtIsNull();

    List<ThesisExaminationStaging> findByLecturerIdAndDeletedAtIsNull(Long lecturerId);

    List<ThesisExaminationStaging> findByThesisIdAndDeletedAtIsNull(String thesisId);

    List<ThesisExaminationStaging> findByExaminationDateBetweenAndDeletedAtIsNull(
            LocalDate startDate, LocalDate endDate);

    List<ThesisExaminationStaging> findByPayrollPeriodUsedIsNullAndDeletedAtIsNull();

    @Query("SELECT t FROM ThesisExaminationStaging t WHERE t.lecturerId = :lecturerId " +
            "AND t.examinationDate BETWEEN :startDate AND :endDate " +
            "AND t.payrollPeriodUsed IS NULL AND t.deletedAt IS NULL")
    List<ThesisExaminationStaging> findByLecturerIdAndDateRangeAndNotProcessed(
            @Param("lecturerId") Long lecturerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM ThesisExaminationStaging t WHERE t.lecturerId = :lecturerId " +
            "AND t.payrollPeriodUsed IS NULL AND t.deletedAt IS NULL")
    List<ThesisExaminationStaging> findUnprocessedByLecturerId(@Param("lecturerId") Long lecturerId);

    @Query("SELECT COUNT(t) FROM ThesisExaminationStaging t WHERE t.lecturerId = :lecturerId " +
            "AND t.examinationMode = :mode AND t.deletedAt IS NULL")
    long countByLecturerIdAndExaminationMode(
            @Param("lecturerId") Long lecturerId,
            @Param("mode") String examinationMode);
}
