package com.hris.repository;

import com.hris.model.LecturerSalary;
import com.hris.model.enums.LecturerSalaryStatus;
import com.hris.model.enums.LecturerEmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerSalaryRepository extends JpaRepository<LecturerSalary, Long> {

    List<LecturerSalary> findAllByDeletedAtIsNullOrderByPeriodDesc();

    List<LecturerSalary> findByLecturerProfileIdAndDeletedAtIsNullOrderByPeriodDesc(Long lecturerProfileId);

    Optional<LecturerSalary> findByLecturerProfileIdAndPeriodAndDeletedAtIsNull(
            Long lecturerProfileId, String period);

    List<LecturerSalary> findByPeriodAndDeletedAtIsNullOrderByLecturerProfileId(String period);

    List<LecturerSalary> findByStatusAndDeletedAtIsNull(LecturerSalaryStatus status);

    List<LecturerSalary> findByLecturerTypeAndDeletedAtIsNull(LecturerEmploymentStatus lecturerType);

    List<LecturerSalary> findByPeriodAndLecturerTypeAndDeletedAtIsNull(
            String period, LecturerEmploymentStatus lecturerType);

    @Query("SELECT ls FROM LecturerSalary ls WHERE ls.period = :period " +
            "AND ls.lecturerType = :lecturerType AND ls.status = :status " +
            "AND ls.deletedAt IS NULL")
    List<LecturerSalary> findByPeriodAndLecturerTypeAndStatus(
            @Param("period") String period,
            @Param("lecturerType") LecturerEmploymentStatus lecturerType,
            @Param("status") LecturerSalaryStatus status);

    @Query("SELECT ls FROM LecturerSalary ls WHERE ls.status = 'CALCULATED' " +
            "AND ls.deletedAt IS NULL ORDER BY ls.period DESC")
    List<LecturerSalary> findCalculatedSalaries();

    @Query("SELECT ls FROM LecturerSalary ls WHERE ls.lecturerProfileId = :lecturerId " +
            "AND ls.period BETWEEN :startPeriod AND :endPeriod AND ls.deletedAt IS NULL " +
            "ORDER BY ls.period DESC")
    List<LecturerSalary> findByLecturerIdAndPeriodRange(
            @Param("lecturerId") Long lecturerId,
            @Param("startPeriod") String startPeriod,
            @Param("endPeriod") String endPeriod);

    @Query("SELECT DISTINCT ls.period FROM LecturerSalary ls WHERE ls.deletedAt IS NULL " +
            "ORDER BY ls.period DESC")
    List<String> findDistinctPeriods();

    boolean existsByLecturerProfileIdAndPeriodAndDeletedAtIsNull(
            Long lecturerProfileId, String period);
}
