package com.hris.repository;

import com.hris.model.LecturerSalaryRate;
import com.hris.model.enums.LecturerRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerSalaryRateRepository extends JpaRepository<LecturerSalaryRate, Long> {

    List<LecturerSalaryRate> findAllByDeletedAtIsNull();

    Optional<LecturerSalaryRate> findByAcademicRankAndDeletedAtIsNull(LecturerRank academicRank);

    boolean existsByAcademicRankAndDeletedAtIsNull(LecturerRank academicRank);

    @Query("SELECT lr FROM LecturerSalaryRate lr WHERE lr.deletedAt IS NULL ORDER BY lr.academicRank")
    List<LecturerSalaryRate> findAllOrderByAcademicRank();
}
