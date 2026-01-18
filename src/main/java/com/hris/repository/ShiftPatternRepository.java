package com.hris.repository;

import com.hris.model.ShiftPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Shift Pattern Repository
 */
@Repository
public interface ShiftPatternRepository extends JpaRepository<ShiftPattern, Long> {

    /**
     * Find by code (active only)
     */
    Optional<ShiftPattern> findByCodeAndDeletedAtIsNull(String code);

    /**
     * Find all active shift patterns ordered by display order
     */
    List<ShiftPattern> findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();

    /**
     * Find with shift package fetched
     */
    @Query("SELECT sp FROM ShiftPattern sp LEFT JOIN FETCH sp.shiftPackage WHERE sp.id = :id AND sp.deletedAt IS NULL")
    Optional<ShiftPattern> findByIdWithShiftPackage(@Param("id") Long id);

    /**
     * Find all with shift package fetched
     */
    @Query("SELECT sp FROM ShiftPattern sp LEFT JOIN FETCH sp.shiftPackage WHERE sp.deletedAt IS NULL ORDER BY sp.displayOrder ASC, sp.name ASC")
    List<ShiftPattern> findAllWithShiftPackage();

    /**
     * Check if code exists
     */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /**
     * Check if code exists excluding current id
     */
    @Query("SELECT COUNT(sp) > 0 FROM ShiftPattern sp WHERE sp.code = :code AND sp.deletedAt IS NULL AND sp.id != :id")
    boolean existsByCodeAndDeletedAtIsNullAndIdNot(@Param("code") String code, @Param("id") Long id);
}
