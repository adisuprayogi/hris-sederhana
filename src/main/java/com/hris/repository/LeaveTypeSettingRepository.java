package com.hris.repository;

import com.hris.model.LeaveTypeSetting;
import com.hris.model.enums.LeaveTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository untuk LeaveTypeSetting Entity
 */
@Repository
public interface LeaveTypeSettingRepository extends JpaRepository<LeaveTypeSetting, Long> {

    /**
     * Find all active leave types by year
     */
    List<LeaveTypeSetting> findByYearAndIsActiveTrueAndDeletedAtIsNullOrderByCode(Integer year);

    /**
     * Find all leave types by year (including inactive)
     */
    List<LeaveTypeSetting> findByYearAndDeletedAtIsNullOrderByCode(Integer year);

    /**
     * Find leave type by code and year
     */
    Optional<LeaveTypeSetting> findByCodeAndYearAndDeletedAtIsNull(String code, Integer year);

    /**
     * Check if leave type code exists for a year
     */
    boolean existsByCodeAndYearAndDeletedAtIsNull(String code, Integer year);

    /**
     * Find by leave type (QUOTA/NO_QUOTA) and year
     */
    List<LeaveTypeSetting> findByLeaveTypeAndYearAndIsActiveTrueAndDeletedAtIsNullOrderByCode(
            LeaveTypeEnum leaveType, Integer year);

    /**
     * Find quota-based leave types for a year
     */
    @Query("SELECT lt FROM LeaveTypeSetting lt WHERE lt.year = :year AND lt.leaveType = 'QUOTA' AND lt.isActive = true AND lt.deletedAt IS NULL ORDER BY lt.code")
    List<LeaveTypeSetting> findQuotaTypesByYear(@Param("year") Integer year);

    /**
     * Find all years that have leave type settings
     */
    @Query("SELECT DISTINCT lt.year FROM LeaveTypeSetting lt WHERE lt.deletedAt IS NULL ORDER BY lt.year DESC")
    List<Integer> findAllDistinctYears();

    /**
     * Count active leave types by year
     */
    long countByYearAndIsActiveTrueAndDeletedAtIsNull(Integer year);

    /**
     * Count quota types by year
     */
    long countByLeaveTypeAndYearAndIsActiveTrueAndDeletedAtIsNull(LeaveTypeEnum leaveType, Integer year);

    /**
     * Find by gender restriction
     */
    List<LeaveTypeSetting> findByGenderRestrictionAndYearAndIsActiveTrueAndDeletedAtIsNullOrderByCode(
            com.hris.model.enums.GenderRestriction genderRestriction, Integer year);
}
