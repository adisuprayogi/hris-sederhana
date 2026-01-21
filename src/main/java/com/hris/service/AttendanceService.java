package com.hris.service;

import com.hris.dto.ClockInRequest;
import com.hris.dto.ClockOutRequest;
import com.hris.model.*;
import com.hris.model.enums.AttendanceStatus;
import com.hris.model.enums.RequestStatus;
import com.hris.repository.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Attendance Service
 * Handles clock in/out with location validation and shift pattern integration
 *
 * Key Features:
 * - Clock in/out with geolocation validation
 * - WFH approval checking
 * - Shift pattern integration for working hours
 * - Late calculation with tolerance from shift pattern
 * - Overtime calculation
 * - Deduction calculation from shift pattern
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeShiftService employeeShiftService;
    private final WfhRequestService wfhRequestService;
    private final CompanyService companyService;
    private final EmployeeService employeeService;
    private final HolidayService holidayService;

    // =====================================================
    // CLOCK IN
    // =====================================================

    /**
     * Clock in with location validation
     *
     * @param request Clock in request with location data
     * @return Created attendance record
     */
    @Transactional
    public AttendanceRecord clockIn(ClockInRequest request) {
        log.info("Clock in for employee: {} at {}", request.getEmployeeId(), request.getClockInDateTime());

        // 1. Get employee
        Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        LocalDate attendanceDate = request.getClockInDateTime().toLocalDate();
        LocalTime clockInTime = request.getClockInDateTime().toLocalTime();

        // 2. Check if already clocked in
        if (attendanceRecordRepository.hasClockedInToday(request.getEmployeeId(), attendanceDate)) {
            throw new IllegalStateException("Already clocked in today");
        }

        // 3. Get shift assignment
        EmployeeShiftService.ShiftAssignmentResult shift =
                employeeShiftService.getShiftAssignment(request.getEmployeeId(), attendanceDate);

        // 4. Validate working day
        if (!shift.isWorkingDay()) {
            throw new IllegalStateException("Cannot clock-in on non-working day");
        }

        // 5. Check holiday override
        if (!shouldAllowClockIn(shift, attendanceDate, employee)) {
            throw new IllegalStateException("Cannot clock-in on holiday (no override)");
        }

        // 6. Check if current time is past shift end time
        if (shift.getEndTime() != null) {
            if (clockInTime.isAfter(shift.getEndTime())) {
                throw new IllegalStateException(
                    String.format("Tidak dapat clock-in. Waktu saat ini (%s) sudah melewati jam shift keluar (%s).",
                        clockInTime, shift.getEndTime())
                );
            }
        }

        // 7. Validate location (if not WFH)
        boolean isWfh = false;
        if (shift.getShiftPattern() != null && Boolean.TRUE.equals(shift.getShiftPattern().getIsWfhAllowed())) {
            // Check if has approved WFH request
            isWfh = wfhRequestService.hasApprovedWfhForDate(request.getEmployeeId(), attendanceDate);
            if (!isWfh) {
                throw new IllegalStateException("Shift allows WFH but no approved WFH request found. Please submit WFH request first.");
            }
        }

        if (!isWfh) {
            validateLocation(request.getLatitude(), request.getLongitude());
        }

        // 8. Calculate late
        int lateMinutes = 0;
        BigDecimal lateDeduction = BigDecimal.ZERO;
        ShiftPattern pattern = shift.getShiftPattern();

        if (shift.getStartTime() != null && pattern != null) {
            LocalTime effectiveStartTime = shift.getStartTime();
            Integer tolerance = shift.getLateToleranceMinutes();
            if (tolerance != null && tolerance > 0) {
                effectiveStartTime = shift.getStartTime().plusMinutes(tolerance);
            }

            if (clockInTime.isAfter(effectiveStartTime)) {
                lateMinutes = (int) ChronoUnit.MINUTES.between(effectiveStartTime, clockInTime);
                // Use existing deduction method from ShiftPattern
                if (pattern != null) {
                    lateDeduction = pattern.calculateLateDeduction(lateMinutes);
                }
            }
        }

        // 8. Create record
        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .attendanceDate(attendanceDate)
                .clockInTime(clockInTime)
                .clockInLatitude(request.getLatitude())
                .clockInLongitude(request.getLongitude())
                .clockInDeviceInfo(request.getDeviceInfo())
                .workingHours(shift.getWorkingHours())
                .shiftPattern(pattern)
                .isLate(lateMinutes > 0)
                .lateDurationMinutes(lateMinutes)
                .lateDeductionAmount(lateDeduction)
                .requiredWorkMinutes(shift.getWorkingHours() != null ?
                        shift.getWorkingHours().getNetWorkDurationMinutes() : 0)
                .status(lateMinutes > 0 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT)
                .isWfh(isWfh)
                .build();

        AttendanceRecord saved = attendanceRecordRepository.save(record);
        log.info("Clock in successful: {}", saved.getId());
        return saved;
    }

    // =====================================================
    // CLOCK OUT
    // =====================================================

    /**
     * Clock out with overtime calculation
     *
     * @param request Clock out request with location data
     * @return Updated attendance record
     */
    @Transactional
    public AttendanceRecord clockOut(ClockOutRequest request) {
        log.info("Clock out for employee: {} at {}", request.getEmployeeId(), request.getClockOutDateTime());

        LocalDate attendanceDate = request.getClockOutDateTime().toLocalDate();
        LocalTime clockOutTime = request.getClockOutDateTime().toLocalTime();

        // 1. Get existing record
        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateAndDeletedAtIsNull(request.getEmployeeId(), attendanceDate)
                .orElseThrow(() -> new IllegalStateException("No clock-in record found"));

        // 2. Check if already clocked out
        if (record.getClockOutTime() != null) {
            throw new IllegalStateException("Already clocked out");
        }

        // 3. Get shift assignment
        EmployeeShiftService.ShiftAssignmentResult shift =
                employeeShiftService.getShiftAssignment(request.getEmployeeId(), attendanceDate);

        // 4. Calculate early leave
        int earlyLeaveMinutes = 0;
        ShiftPattern pattern = shift.getShiftPattern();
        WorkingHours wh = shift.getWorkingHours();

        if (wh != null && wh.getEndTime() != null && pattern != null) {
            LocalTime effectiveEndTime = wh.getEndTime();
            Integer tolerance = pattern.getEarlyLeaveToleranceMinutes();
            if (tolerance != null && tolerance > 0) {
                effectiveEndTime = effectiveEndTime.minusMinutes(tolerance);
            }

            if (clockOutTime.isBefore(effectiveEndTime)) {
                earlyLeaveMinutes = (int) ChronoUnit.MINUTES.between(clockOutTime, effectiveEndTime);
            }
        }

        // 5. Calculate overtime
        int overtimeMinutes = 0;
        if (wh != null && wh.getEndTime() != null && pattern != null) {
            // Check if overtime allowed
            if (Boolean.TRUE.equals(pattern.getIsOvertimeAllowed())) {
                if (clockOutTime.isAfter(wh.getEndTime())) {
                    overtimeMinutes = (int) ChronoUnit.MINUTES.between(wh.getEndTime(), clockOutTime);
                }
            }
        }

        // 6. Calculate work duration & underwork
        int actualWorkMinutes = 0;
        int underworkMinutes = 0;
        BigDecimal underworkDeduction = BigDecimal.ZERO;

        if (record.getClockInTime() != null && wh != null) {
            actualWorkMinutes = (int) ChronoUnit.MINUTES.between(
                    record.getClockInTime(), clockOutTime);

            int requiredMinutes = wh.getNetWorkDurationMinutes();
            if (actualWorkMinutes < requiredMinutes) {
                underworkMinutes = requiredMinutes - actualWorkMinutes;
                // Use existing deduction method from ShiftPattern
                if (pattern != null) {
                    underworkDeduction = pattern.calculateUnderworkDeduction(underworkMinutes);
                }
            }
        }

        // 7. Update record
        record.setClockOutTime(clockOutTime);
        record.setClockOutLatitude(request.getLatitude());
        record.setClockOutLongitude(request.getLongitude());
        record.setClockOutDeviceInfo(request.getDeviceInfo());
        record.setIsEarlyLeave(earlyLeaveMinutes > 0);
        record.setEarlyLeaveDurationMinutes(earlyLeaveMinutes);
        record.setIsOvertime(overtimeMinutes > 0);
        record.setOvertimeDurationMinutes(overtimeMinutes);
        record.setActualWorkMinutes(actualWorkMinutes);
        record.setUnderworkMinutes(underworkMinutes);
        record.setUnderworkDeductionAmount(underworkDeduction);

        AttendanceRecord saved = attendanceRecordRepository.save(record);
        log.info("Clock out successful: {}", saved.getId());
        return saved;
    }

    // =====================================================
    // QUERY METHODS
    // =====================================================

    /**
     * Get attendance record by employee and date
     */
    @Transactional(readOnly = true)
    public AttendanceRecord getAttendanceByEmployeeAndDate(Long employeeId, LocalDate date) {
        return attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateAndDeletedAtIsNull(employeeId, date)
                .orElse(null);
    }

    /**
     * Get attendance records by employee and date range
     */
    @Transactional(readOnly = true)
    public java.util.List<AttendanceRecord> getAttendanceByEmployeeAndDateRange(
            Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateBetweenAndDeletedAtIsNullOrderByAttendanceDateDesc(
                        employeeId, startDate, endDate);
    }

    /**
     * Get today's attendance status for employee
     */
    @Transactional(readOnly = true)
    public AttendanceRecord getTodayAttendance(Long employeeId) {
        return getAttendanceByEmployeeAndDate(employeeId, LocalDate.now());
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Validate clock-in location against office location
     */
    private void validateLocation(BigDecimal latitude, BigDecimal longitude) {
        Company company = companyService.getCompany();
        if (company == null) {
            throw new IllegalStateException("Company not found");
        }

        if (company.getOfficeLatitude() == null || company.getOfficeLongitude() == null) {
            log.warn("Office location not set, skipping location validation");
            return;
        }

        double distance = calculateDistance(
                latitude.doubleValue(),
                longitude.doubleValue(),
                company.getOfficeLatitude().doubleValue(),
                company.getOfficeLongitude().doubleValue()
        );

        int maxRadius = company.getAttendanceLocationRadius() != null ?
                company.getAttendanceLocationRadius() : 100;

        if (distance > maxRadius) {
            throw new IllegalStateException(
                    String.format("Location validation failed. You are %d meters from office (max: %d meters)",
                            (int) distance, maxRadius));
        }

        log.info("Location validation successful: {} meters from office", (int) distance);
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371_000; // Earth radius in meters

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Check if clock-in is allowed considering holiday override settings
     */
    private boolean shouldAllowClockIn(EmployeeShiftService.ShiftAssignmentResult shift,
                                       LocalDate date, Employee employee) {
        // Check if this date is a holiday
        Holiday holiday = holidayService.getHolidayByDate(date).orElse(null);
        if (holiday == null) {
            return true; // Not a holiday, allow clock-in
        }

        ShiftPattern pattern = shift.getShiftPattern();
        if (pattern == null) {
            return false; // No pattern, default to holiday rule
        }

        // Check holiday override settings from ShiftPattern
        if (holiday.isNationalHoliday() && Boolean.TRUE.equals(pattern.getOverrideNationalHoliday())) {
            return true;
        }
        if (holiday.isCompanyHoliday() && Boolean.TRUE.equals(pattern.getOverrideCompanyHoliday())) {
            return true;
        }
        if (holiday.isCollectiveLeave() && Boolean.TRUE.equals(pattern.getOverrideJointLeave())) {
            return true;
        }

        return false; // Holiday and no override, reject clock-in
    }
}
