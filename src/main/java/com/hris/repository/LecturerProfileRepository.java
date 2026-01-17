package com.hris.repository;

import com.hris.model.LecturerProfile;
import com.hris.model.enums.LecturerEmploymentStatus;
import com.hris.model.enums.LecturerRank;
import com.hris.model.enums.LecturerWorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LecturerProfile entity
 */
@Repository
public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, Long> {

    /**
     * Find active lecturer profile by employee ID
     */
    Optional<LecturerProfile> findByEmployeeIdAndDeletedAtIsNull(Long employeeId);

    /**
     * Find lecturer profile by NIDN
     */
    Optional<LecturerProfile> findByNidnAndDeletedAtIsNull(String nidn);

    /**
     * Find lecturer profile by ID with employee eagerly fetched
     * Used for detail page to avoid LazyInitializationException
     */
    @Query("SELECT lp FROM LecturerProfile lp " +
           "LEFT JOIN FETCH lp.employee e " +
           "LEFT JOIN FETCH e.department d " +
           "LEFT JOIN FETCH e.position p " +
           "LEFT JOIN FETCH lp.homebaseProdi " +
           "WHERE lp.id = :id AND lp.deletedAt IS NULL")
    Optional<LecturerProfile> findByIdWithEmployee(@Param("id") Long id);

    /**
     * Check if NIDN exists
     */
    boolean existsByNidnAndDeletedAtIsNull(String nidn);

    /**
     * Check if NIDN exists (excluding current)
     */
    boolean existsByNidnAndDeletedAtIsNullAndIdNot(String nidn, Long id);

    /**
     * Find all active lecturers
     */
    @Query("SELECT DISTINCT lp FROM LecturerProfile lp " +
           "LEFT JOIN FETCH lp.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH lp.homebaseProdi " +
           "WHERE lp.deletedAt IS NULL ORDER BY e.fullName")
    List<LecturerProfile> findAllActive();

    /**
     * Find by homebase prodi
     */
    @Query("SELECT DISTINCT lp FROM LecturerProfile lp " +
           "LEFT JOIN FETCH lp.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH lp.homebaseProdi " +
           "WHERE lp.homebaseProdi.id = :prodiId AND lp.deletedAt IS NULL " +
           "ORDER BY e.fullName")
    List<LecturerProfile> findByHomebaseProdiIdAndDeletedAtIsNull(@Param("prodiId") Long prodiId);

    /**
     * Find by employment status
     */
    @Query("SELECT DISTINCT lp FROM LecturerProfile lp " +
           "LEFT JOIN FETCH lp.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH lp.homebaseProdi " +
           "WHERE lp.employmentStatus = :status AND lp.deletedAt IS NULL " +
           "ORDER BY e.fullName")
    List<LecturerProfile> findByEmploymentStatusAndDeletedAtIsNull(@Param("status") LecturerEmploymentStatus status);

    /**
     * Find by work status
     */
    @Query("SELECT DISTINCT lp FROM LecturerProfile lp " +
           "LEFT JOIN FETCH lp.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH lp.homebaseProdi " +
           "WHERE lp.workStatus = :status AND lp.deletedAt IS NULL " +
           "ORDER BY e.fullName")
    List<LecturerProfile> findByWorkStatusAndDeletedAtIsNull(@Param("status") LecturerWorkStatus status);

    /**
     * Find by rank
     */
    @Query("SELECT DISTINCT lp FROM LecturerProfile lp " +
           "LEFT JOIN FETCH lp.employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH lp.homebaseProdi " +
           "WHERE lp.lecturerRank = :rank AND lp.deletedAt IS NULL " +
           "ORDER BY e.fullName")
    List<LecturerProfile> findByLecturerRankAndDeletedAtIsNull(@Param("rank") LecturerRank rank);

    /**
     * Search lecturers with filters
     */
    @Query("SELECT DISTINCT lp FROM LecturerProfile lp " +
           "LEFT JOIN FETCH lp.employee e " +
           "LEFT JOIN FETCH e.department d " +
           "LEFT JOIN FETCH e.position p " +
           "LEFT JOIN FETCH lp.homebaseProdi hp " +
           "WHERE (:search IS NULL OR " +
           "  LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(e.nik) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(lp.nidn) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "  (:rank IS NULL OR lp.lecturerRank = :rank) AND " +
           "  (:empStatus IS NULL OR lp.employmentStatus = :empStatus) AND " +
           "  (:workStatus IS NULL OR lp.workStatus = :workStatus) AND " +
           "  (:prodiId IS NULL OR lp.homebaseProdi.id = :prodiId) AND " +
           "  lp.deletedAt IS NULL " +
           "ORDER BY e.fullName")
    List<LecturerProfile> searchLecturers(
            @Param("search") String search,
            @Param("rank") LecturerRank rank,
            @Param("empStatus") LecturerEmploymentStatus empStatus,
            @Param("workStatus") LecturerWorkStatus workStatus,
            @Param("prodiId") Long prodiId
    );

    /**
     * Count by homebase prodi
     */
    @Query("SELECT COUNT(lp) FROM LecturerProfile lp " +
           "WHERE lp.homebaseProdi.id = :prodiId AND lp.deletedAt IS NULL")
    long countByHomebaseProdiIdAndDeletedAtIsNull(@Param("prodiId") Long prodiId);

    /**
     * Count by employment status
     */
    @Query("SELECT COUNT(lp) FROM LecturerProfile lp " +
           "WHERE lp.employmentStatus = :status AND lp.deletedAt IS NULL")
    long countByEmploymentStatusAndDeletedAtIsNull(@Param("status") LecturerEmploymentStatus status);

    /**
     * Count by work status
     */
    @Query("SELECT COUNT(lp) FROM LecturerProfile lp " +
           "WHERE lp.workStatus = :status AND lp.deletedAt IS NULL")
    long countByWorkStatusAndDeletedAtIsNull(@Param("status") LecturerWorkStatus status);

    /**
     * Count all active lecturers
     */
    long countByDeletedAtIsNull();
}
