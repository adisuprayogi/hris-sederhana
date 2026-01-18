package com.hris.repository;

import com.hris.model.ShiftPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Shift Package Repository
 */
@Repository
public interface ShiftPackageRepository extends JpaRepository<ShiftPackage, Long> {

    /**
     * Find by code (active only)
     */
    Optional<ShiftPackage> findByCodeAndDeletedAtIsNull(String code);

    /**
     * Find all active shift packages ordered by display order
     */
    List<ShiftPackage> findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();

    /**
     * Find with working hours fetched
     */
    @Query("SELECT sp FROM ShiftPackage sp LEFT JOIN FETCH sp.sundayWorkingHours LEFT JOIN FETCH sp.mondayWorkingHours LEFT JOIN FETCH sp.tuesdayWorkingHours LEFT JOIN FETCH sp.wednesdayWorkingHours LEFT JOIN FETCH sp.thursdayWorkingHours LEFT JOIN FETCH sp.fridayWorkingHours LEFT JOIN FETCH sp.saturdayWorkingHours WHERE sp.id = :id AND sp.deletedAt IS NULL")
    Optional<ShiftPackage> findByIdWithWorkingHours(@Param("id") Long id);

    /**
     * Find all with working hours fetched
     */
    @Query("SELECT sp FROM ShiftPackage sp LEFT JOIN FETCH sp.sundayWorkingHours LEFT JOIN FETCH sp.mondayWorkingHours LEFT JOIN FETCH sp.tuesdayWorkingHours LEFT JOIN FETCH sp.wednesdayWorkingHours LEFT JOIN FETCH sp.thursdayWorkingHours LEFT JOIN FETCH sp.fridayWorkingHours LEFT JOIN FETCH sp.saturdayWorkingHours WHERE sp.deletedAt IS NULL ORDER BY sp.displayOrder ASC, sp.name ASC")
    List<ShiftPackage> findAllWithWorkingHours();

    /**
     * Check if code exists
     */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /**
     * Check if code exists excluding current id
     */
    @Query("SELECT COUNT(sp) > 0 FROM ShiftPackage sp WHERE sp.code = :code AND sp.deletedAt IS NULL AND sp.id != :id")
    boolean existsByCodeAndDeletedAtIsNullAndIdNot(@Param("code") String code, @Param("id") Long id);
}
