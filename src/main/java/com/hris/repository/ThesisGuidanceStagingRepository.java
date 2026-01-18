package com.hris.repository;

import com.hris.model.ThesisGuidanceStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThesisGuidanceStagingRepository extends JpaRepository<ThesisGuidanceStaging, Long> {

    List<ThesisGuidanceStaging> findAllByDeletedAtIsNull();

    List<ThesisGuidanceStaging> findByLecturerIdAndDeletedAtIsNull(Long lecturerId);

    List<ThesisGuidanceStaging> findByStudentIdAndDeletedAtIsNull(String studentId);

    List<ThesisGuidanceStaging> findByThesisTypeAndDeletedAtIsNull(String thesisType);

    List<ThesisGuidanceStaging> findByPayrollPeriodUsedIsNullAndDeletedAtIsNull();

    @Query("SELECT t FROM ThesisGuidanceStaging t WHERE t.lecturerId = :lecturerId " +
            "AND t.payrollPeriodUsed IS NULL AND t.deletedAt IS NULL")
    List<ThesisGuidanceStaging> findUnprocessedByLecturerId(@Param("lecturerId") Long lecturerId);

    @Query("SELECT COUNT(t) FROM ThesisGuidanceStaging t WHERE t.lecturerId = :lecturerId " +
            "AND t.guidanceMode = :mode AND t.deletedAt IS NULL")
    long countByLecturerIdAndGuidanceMode(
            @Param("lecturerId") Long lecturerId,
            @Param("mode") String guidanceMode);
}
