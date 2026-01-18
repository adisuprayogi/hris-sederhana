package com.hris.repository;

import com.hris.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Working Hours Repository
 */
@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {

    /**
     * Find by code (active only)
     */
    Optional<WorkingHours> findByCodeAndDeletedAtIsNull(String code);

    /**
     * Find all active working hours ordered by display order
     */
    List<WorkingHours> findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();

    /**
     * Check if code exists
     */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /**
     * Check if code exists excluding current id
     */
    @Query("SELECT COUNT(w) > 0 FROM WorkingHours w WHERE w.code = :code AND w.deletedAt IS NULL AND w.id != :id")
    boolean existsByCodeAndDeletedAtIsNullAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * Find OFF working hours
     */
    @Query("SELECT w FROM WorkingHours w WHERE w.code = 'WH_OFF' AND w.deletedAt IS NULL")
    Optional<WorkingHours> findOffWorkingHours();
}
