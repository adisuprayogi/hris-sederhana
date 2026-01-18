package com.hris.service;

import com.hris.model.Holiday;
import com.hris.model.enums.HolidayType;
import com.hris.repository.HolidayRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

/**
 * Service untuk Holiday Entity
 * Menangani operasi CRUD dan logika bisnis terkait hari libur
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayService {

    private final HolidayRepository holidayRepository;

    /**
     * Get all holidays ordered by date
     */
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll(
                Sort.by(Sort.Direction.ASC, "date")
        );
    }

    /**
     * Get all active holidays ordered by date
     */
    public List<Holiday> getActiveHolidays() {
        return holidayRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByDate();
    }

    /**
     * Get holiday by ID
     */
    public Optional<Holiday> getHolidayById(Long id) {
        return holidayRepository.findById(id);
    }

    /**
     * Get holiday by date
     */
    public Optional<Holiday> getHolidayByDate(LocalDate date) {
        return holidayRepository.findByDateAndDeletedAtIsNull(date);
    }

    /**
     * Get holidays by year
     */
    public List<Holiday> getHolidaysByYear(Integer year) {
        return holidayRepository.findByYearAndDeletedAtIsNullOrderByDate(year);
    }

    /**
     * Get holidays by year and type
     */
    public List<Holiday> getHolidaysByYearAndType(Integer year, HolidayType holidayType) {
        return holidayRepository.findByYearAndHolidayTypeAndDeletedAtIsNullOrderByDate(year, holidayType);
    }

    /**
     * Get holidays by type
     */
    public List<Holiday> getHolidaysByType(HolidayType holidayType) {
        return holidayRepository.findByHolidayTypeAndIsActiveTrueAndDeletedAtIsNullOrderByDate(holidayType);
    }

    /**
     * Get holidays in date range
     */
    public List<Holiday> getHolidaysBetween(LocalDate startDate, LocalDate endDate) {
        return holidayRepository.findByDateBetweenAndIsActiveTrueAndDeletedAtIsNullOrderByDate(startDate, endDate);
    }

    /**
     * Create new holiday
     */
    @Transactional
    public Holiday createHoliday(Holiday holiday) {
        // Validate: check if date already exists
        if (holidayRepository.existsByDateAndDeletedAtIsNull(holiday.getDate())) {
            throw new IllegalArgumentException(
                    "Holiday with date " + holiday.getDate() + " already exists"
            );
        }

        // Set year from date if not set
        if (holiday.getYear() == null) {
            holiday.setYear(holiday.getDate().getYear());
        }

        Holiday saved = holidayRepository.save(holiday);
        log.info("Created new holiday: {} on {}", saved.getName(), saved.getDate());
        return saved;
    }

    /**
     * Update holiday
     */
    @Transactional
    public Holiday updateHoliday(Long id, Holiday holidayDetails) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with id: " + id));

        // Validate: check if date conflict with other holiday
        Optional<Holiday> existingByDate = holidayRepository.findByDateAndDeletedAtIsNull(holidayDetails.getDate());
        if (existingByDate.isPresent() && !existingByDate.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "Another holiday with date " + holidayDetails.getDate() + " already exists"
            );
        }

        // Update fields
        holiday.setName(holidayDetails.getName());
        holiday.setDate(holidayDetails.getDate());
        holiday.setYear(holidayDetails.getYear() != null ? holidayDetails.getYear() : holidayDetails.getDate().getYear());
        holiday.setHolidayType(holidayDetails.getHolidayType());
        holiday.setIsActive(holidayDetails.getIsActive() != null ? holidayDetails.getIsActive() : true);
        holiday.setDescription(holidayDetails.getDescription());
        holiday.setRepeatAnnually(holidayDetails.getRepeatAnnually() != null ? holidayDetails.getRepeatAnnually() : false);

        Holiday updated = holidayRepository.save(holiday);
        log.info("Updated holiday: {} on {}", updated.getName(), updated.getDate());
        return updated;
    }

    /**
     * Delete holiday (soft delete)
     */
    @Transactional
    public void deleteHoliday(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with id: " + id));

        holiday.setDeletedAt(java.time.LocalDateTime.now());
        holidayRepository.save(holiday);
        log.info("Deleted holiday: {} on {}", holiday.getName(), holiday.getDate());
    }

    /**
     * Check if date is a holiday
     */
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.isHoliday(date);
    }

    /**
     * Check if date is a national holiday
     */
    public boolean isNationalHoliday(LocalDate date) {
        return holidayRepository.findByDateAndDeletedAtIsNull(date)
                .map(Holiday::isNationalHoliday)
                .orElse(false);
    }

    /**
     * Check if date is a company holiday
     */
    public boolean isCompanyHoliday(LocalDate date) {
        return holidayRepository.findByDateAndDeletedAtIsNull(date)
                .map(Holiday::isCompanyHoliday)
                .orElse(false);
    }

    /**
     * Check if date is collective leave
     */
    public boolean isCollectiveLeave(LocalDate date) {
        return holidayRepository.findByDateAndDeletedAtIsNull(date)
                .map(Holiday::isCollectiveLeave)
                .orElse(false);
    }

    /**
     * Get holidays for a specific month
     */
    public List<Holiday> getHolidaysForMonth(int year, Month month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return holidayRepository.findByDateBetweenAndIsActiveTrueAndDeletedAtIsNullOrderByDate(
                startDate, endDate);
    }

    /**
     * Get all repeatable holidays
     */
    public List<Holiday> getRepeatableHolidays() {
        return holidayRepository.findByRepeatAnnuallyTrueAndIsActiveTrueAndDeletedAtIsNullOrderByDate();
    }

    /**
     * Get next holiday from today
     */
    public Optional<Holiday> getNextHoliday() {
        LocalDate today = LocalDate.now();
        List<Holiday> upcomingHolidays = holidayRepository
                .findByDateBetweenAndIsActiveTrueAndDeletedAtIsNullOrderByDate(
                        today, today.plusYears(1));
        return upcomingHolidays.stream().findFirst();
    }

    /**
     * Get upcoming holidays in next N days
     */
    public List<Holiday> getUpcomingHolidays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return holidayRepository.findByDateBetweenAndIsActiveTrueAndDeletedAtIsNullOrderByDate(
                today, endDate);
    }

    /**
     * Get holiday statistics
     */
    public HolidayStats getHolidayStats() {
        List<Holiday> allHolidays = getActiveHolidays();

        long nationalCount = allHolidays.stream()
                .filter(Holiday::isNationalHoliday)
                .count();

        long companyCount = allHolidays.stream()
                .filter(Holiday::isCompanyHoliday)
                .count();

        long collectiveLeaveCount = allHolidays.stream()
                .filter(Holiday::isCollectiveLeave)
                .count();

        long repeatableCount = allHolidays.stream()
                .filter(h -> h.getRepeatAnnually())
                .count();

        return new HolidayStats(allHolidays.size(), nationalCount, companyCount,
                collectiveLeaveCount, repeatableCount);
    }

    /**
     * DTO for holiday statistics
     */
    public record HolidayStats(
            long totalHolidays,
            long nationalHolidays,
            long companyHolidays,
            long collectiveLeave,
            long repeatableHolidays
    ) {}
}
