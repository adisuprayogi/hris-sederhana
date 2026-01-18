package com.hris.repository;

import com.hris.model.Holiday;
import com.hris.model.enums.HolidayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk Holiday Entity
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    /**
     * Find holiday by specific date
     */
    Optional<Holiday> findByDateAndDeletedAtIsNull(LocalDate date);

    /**
     * Find all active holidays in a date range
     */
    List<Holiday> findByDateBetweenAndIsActiveTrueAndDeletedAtIsNullOrderByDate(
            LocalDate startDate, LocalDate endDate);

    /**
     * Find all holidays by year
     */
    List<Holiday> findByYearAndDeletedAtIsNullOrderByDate(Integer year);

    /**
     * Find all holidays by year and type
     */
    List<Holiday> findByYearAndHolidayTypeAndDeletedAtIsNullOrderByDate(Integer year, HolidayType holidayType);

    /**
     * Find all holidays by type
     */
    List<Holiday> findByHolidayTypeAndIsActiveTrueAndDeletedAtIsNullOrderByDate(
            HolidayType holidayType);

    /**
     * Find all active holidays ordered by date
     */
    List<Holiday> findByIsActiveTrueAndDeletedAtIsNullOrderByDate();

    /**
     * Check if date is a holiday
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Holiday h " +
            "WHERE h.date = :date AND h.isActive = true AND h.deletedAt IS NULL")
    boolean isHoliday(@Param("date") LocalDate date);

    /**
     * Find repeatable holidays (repeat_annually = true)
     */
    List<Holiday> findByRepeatAnnuallyTrueAndIsActiveTrueAndDeletedAtIsNullOrderByDate();

    /**
     * Find holidays in date range by type
     */
    List<Holiday> findByDateBetweenAndHolidayTypeAndIsActiveTrueAndDeletedAtIsNullOrderByDate(
            LocalDate startDate, LocalDate endDate, HolidayType holidayType);

    /**
     * Check if holiday exists by date
     */
    boolean existsByDateAndDeletedAtIsNull(LocalDate date);
}
