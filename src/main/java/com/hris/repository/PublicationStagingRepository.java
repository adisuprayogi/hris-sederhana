package com.hris.repository;

import com.hris.model.PublicationStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationStagingRepository extends JpaRepository<PublicationStaging, Long> {

    List<PublicationStaging> findAllByDeletedAtIsNull();

    List<PublicationStaging> findByLecturerIdAndDeletedAtIsNull(Long lecturerId);

    List<PublicationStaging> findBySintaLevelAndDeletedAtIsNull(String sintaLevel);

    List<PublicationStaging> findByIsScopusIndexedTrueAndDeletedAtIsNull();

    List<PublicationStaging> findByIsScopusIndexedTrueAndScopusQuartileAndDeletedAtIsNull(String quartile);

    List<PublicationStaging> findByIsProcessedFalseAndDeletedAtIsNull();

    @Query("SELECT p FROM PublicationStaging p WHERE p.lecturerId = :lecturerId " +
            "AND p.isProcessed = false AND p.deletedAt IS NULL")
    List<PublicationStaging> findUnprocessedByLecturerId(@Param("lecturerId") Long lecturerId);

    @Query("SELECT p FROM PublicationStaging p WHERE p.isScopusIndexed = true " +
            "AND p.isProcessed = false AND p.deletedAt IS NULL ORDER BY p.scopusQuartile")
    List<PublicationStaging> findUnprocessedScopusPublications();

    @Query("SELECT p FROM PublicationStaging p WHERE p.sintaLevel IS NOT NULL " +
            "AND p.sintaLevel != 'NONE' AND p.isProcessed = false AND p.deletedAt IS NULL " +
            "ORDER BY p.sintaLevel")
    List<PublicationStaging> findUnprocessedSintaPublications();

    @Query("SELECT COUNT(p) FROM PublicationStaging p WHERE p.lecturerId = :lecturerId " +
            "AND p.isProcessed = false AND p.deletedAt IS NULL")
    long countUnprocessedByLecturerId(@Param("lecturerId") Long lecturerId);
}
