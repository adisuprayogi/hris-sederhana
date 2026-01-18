package com.hris.repository;

import com.hris.model.ResearchStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResearchStagingRepository extends JpaRepository<ResearchStaging, Long> {

    List<ResearchStaging> findAllByDeletedAtIsNull();

    List<ResearchStaging> findByLecturerIdAndDeletedAtIsNull(Long lecturerId);

    List<ResearchStaging> findByResearchTierAndDeletedAtIsNull(String researchTier);

    List<ResearchStaging> findByIsProcessedFalseAndDeletedAtIsNull();

    @Query("SELECT r FROM ResearchStaging r WHERE r.lecturerId = :lecturerId " +
            "AND r.isProcessed = false AND r.deletedAt IS NULL")
    List<ResearchStaging> findUnprocessedByLecturerId(@Param("lecturerId") Long lecturerId);

    @Query("SELECT r FROM ResearchStaging r WHERE r.isProcessed = false " +
            "AND r.researchTier = :tier AND r.deletedAt IS NULL")
    List<ResearchStaging> findUnprocessedByTier(@Param("tier") String tier);

    @Query("SELECT COUNT(r) FROM ResearchStaging r WHERE r.lecturerId = :lecturerId " +
            "AND r.isProcessed = false AND r.deletedAt IS NULL")
    long countUnprocessedByLecturerId(@Param("lecturerId") Long lecturerId);
}
