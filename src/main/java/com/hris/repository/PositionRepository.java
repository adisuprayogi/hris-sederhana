package com.hris.repository;

import com.hris.model.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Position entity
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    /**
     * Find all active positions
     */
    @Query("SELECT p FROM Position p WHERE p.deletedAt IS NULL ORDER BY p.level, p.name")
    List<Position> findAllActive();

    /**
     * Find positions by level
     */
    @Query("SELECT p FROM Position p WHERE p.level = :level AND p.deletedAt IS NULL ORDER BY p.name")
    List<Position> findByLevelAndDeletedAtIsNull(@Param("level") Integer level);

    /**
     * Find position by name (case-insensitive)
     */
    @Query("SELECT p FROM Position p WHERE LOWER(p.name) = LOWER(:name) AND p.deletedAt IS NULL")
    Optional<Position> findByNameIgnoreCaseAndDeletedAtIsNull(@Param("name") String name);

    /**
     * Check if position name exists
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Position p WHERE LOWER(p.name) = LOWER(:name) AND p.deletedAt IS NULL")
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(@Param("name") String name);

    /**
     * Search positions with filters and pagination
     */
    @Query("SELECT p FROM Position p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:level IS NULL OR p.level = :level) AND " +
           "p.deletedAt IS NULL")
    Page<Position> searchPositions(
            @Param("search") String search,
            @Param("level") Integer level,
            Pageable pageable
    );

    /**
     * Find positions by minimum level
     */
    @Query("SELECT p FROM Position p WHERE p.level >= :minLevel AND p.deletedAt IS NULL ORDER BY p.level, p.name")
    List<Position> findByLevelGreaterThanEqualAndDeletedAtIsNull(@Param("minLevel") Integer minLevel);

    /**
     * Count positions by level
     */
    @Query("SELECT COUNT(p) FROM Position p WHERE p.level = :level AND p.deletedAt IS NULL")
    long countByLevelAndDeletedAtIsNull(@Param("level") Integer level);
}
