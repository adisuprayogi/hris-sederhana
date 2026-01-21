package com.hris.controller;

import com.hris.dto.ClockInRequest;
import com.hris.dto.ClockOutRequest;
import com.hris.model.*;
import com.hris.repository.AttendanceRecordRepository;
import com.hris.repository.EmployeeRepository;
import com.hris.service.AttendanceService;
import com.hris.service.CompanyService;
import com.hris.service.EmployeeShiftService;
import com.hris.service.HolidayService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Attendance Controller
 * Handles attendance clock in/out and management
 */
@Slf4j
@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeShiftService employeeShiftService;
    private final HolidayService holidayService;
    private final CompanyService companyService;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    // =====================================================
    // PAGES
    // =====================================================

    /**
     * Index page - redirects to clock page
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String index() {
        return "redirect:/attendance/clock";
    }

    /**
     * Clock in/out page
     */
    @GetMapping("/clock")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String clockPage(Model model, Principal principal) {
        // Get current employee from principal
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                .orElse(null);
        if (employee == null) {
            return "redirect:/login";
        }

        // Get today's attendance
        AttendanceRecord todayAttendance = attendanceService.getTodayAttendance(employee.getId());

        model.addAttribute("employee", employee);
        model.addAttribute("todayAttendance", todayAttendance);
        model.addAttribute("activePage", "attendance");
        return "attendance/clock";
    }

    /**
     * Attendance history page
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String historyPage(
            @RequestParam(required = false) @DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model,
            Principal principal) {

        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                .orElse(null);
        if (employee == null) {
            return "redirect:/login";
        }

        // Default to current month if no date range specified
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }

        List<AttendanceRecord> records = attendanceService.getAttendanceByEmployeeAndDateRange(
                employee.getId(), startDate, endDate);

        model.addAttribute("employee", employee);
        model.addAttribute("records", records);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activePage", "attendance");
        return "attendance/history";
    }

    /**
     * Attendance report page (admin/HR only)
     */
    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String reportPage(
            @RequestParam(required = false) @DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long employeeId,
            Model model) {

        // Default to current month if no date range specified
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }

        if (employeeId != null) {
            List<AttendanceRecord> records = attendanceService.getAttendanceByEmployeeAndDateRange(
                    employeeId, startDate, endDate);
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            model.addAttribute("records", records);
            model.addAttribute("selectedEmployee", employee);
        }

        List<Employee> employees = employeeRepository.findAllByDeletedAtIsNullOrderByFullNameAsc();
        model.addAttribute("employees", employees);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activePage", "attendance-report");
        return "attendance/report";
    }

    // =====================================================
    // API ENDPOINTS
    // =====================================================

    /**
     * Clock in API
     */
    @PostMapping("/api/clock-in")
    @ResponseBody
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String clockIn(@RequestBody ClockInRequest request) {
        AttendanceRecord record = attendanceService.clockIn(request);
        return "{\"success\":true,\"id\":" + record.getId() + "}";
    }

    /**
     * Clock out API
     */
    @PostMapping("/api/clock-out")
    @ResponseBody
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String clockOut(@RequestBody ClockOutRequest request) {
        AttendanceRecord record = attendanceService.clockOut(request);
        return "{\"success\":true,\"id\":" + record.getId() + "}";
    }

    /**
     * Get today's attendance status
     */
    @GetMapping("/api/today")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public String getTodayStatus(Principal principal) {
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                .orElse(null);
        if (employee == null) {
            return "{\"success\":false}";
        }
        AttendanceRecord record = attendanceService.getTodayAttendance(employee.getId());
        if (record == null) {
            return "{\"success\":true,\"clockedIn\":false}";
        }
        return "{\"success\":true,\"clockedIn\":true,\"clockedOut\":" + (record.getClockOutTime() != null) + "}";
    }

    /**
     * Get today's shift and validation settings for clock-in page
     * Returns shift info and company geotag settings
     */
    @GetMapping("/api/today-settings")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public String getTodaySettings(Principal principal) {
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                .orElse(null);
        if (employee == null) {
            return "{\"success\":false,\"message\":\"Employee not found\"}";
        }

        LocalDate today = LocalDate.now();

        // Get shift assignment
        EmployeeShiftService.ShiftAssignmentResult shift =
                employeeShiftService.getShiftAssignment(employee.getId(), today);

        // Get company settings
        Company company = companyService.getCompany();

        // Debug logging
        log.info("Company: {}", company != null ? company.getId() : "null");
        if (company != null) {
            log.info("Office lat/lng: {} / {}", company.getOfficeLatitude(), company.getOfficeLongitude());
            log.info("Radius: {}", company.getAttendanceLocationRadius());
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true");

        // Shift info
        if (shift.getShiftPattern() != null) {
            json.append(",\"shiftName\":\"").append(escapeJson(shift.getShiftPattern().getName())).append("\"");
            json.append(",\"shiftColor\":\"").append(escapeJson(shift.getShiftPattern().getColor())).append("\"");
            json.append(",\"isWfhAllowed\":").append(Boolean.TRUE.equals(shift.getShiftPattern().getIsWfhAllowed()));
            json.append(",\"isAttendanceMandatory\":").append(Boolean.TRUE.equals(shift.getShiftPattern().getIsAttendanceMandatory()));
        } else {
            json.append(",\"shiftName\":null");
            json.append(",\"isWfhAllowed\":false");
            json.append(",\"isAttendanceMandatory\":true");
        }

        // Working hours
        if (shift.getStartTime() != null) {
            json.append(",\"shiftStartTime\":\"").append(shift.getStartTime()).append("\"");
        } else {
            json.append(",\"shiftStartTime\":null");
        }
        if (shift.getEndTime() != null) {
            json.append(",\"shiftEndTime\":\"").append(shift.getEndTime()).append("\"");
        } else {
            json.append(",\"shiftEndTime\":null");
        }

        // Working day
        json.append(",\"isWorkingDay\":").append(shift.isWorkingDay());

        // Company geotag settings
        if (company != null) {
            if (company.getOfficeLatitude() != null) {
                json.append(",\"officeLatitude\":").append(company.getOfficeLatitude().toString());
            } else {
                json.append(",\"officeLatitude\":null");
            }
            if (company.getOfficeLongitude() != null) {
                json.append(",\"officeLongitude\":").append(company.getOfficeLongitude().toString());
            } else {
                json.append(",\"officeLongitude\":null");
            }
            json.append(",\"maxRadius\":").append(company.getAttendanceLocationRadius() != null ? company.getAttendanceLocationRadius() : 100);
        } else {
            json.append(",\"officeLatitude\":null");
            json.append(",\"officeLongitude\":null");
            json.append(",\"maxRadius\":100");
        }

        json.append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * Get attendance schedule with shift info (for history page)
     */
    @GetMapping("/api/schedule")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public List<DayScheduleDTO> getAttendanceSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal) {
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                .orElse(null);
        if (employee == null) {
            return Collections.emptyList();
        }

        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateBetweenAndDeletedAtIsNullOrderByAttendanceDateDesc(
                        employee.getId(), startDate, endDate);

        // Create a map of attendance records by date for quick lookup
        Map<LocalDate, AttendanceRecord> recordMap = records.stream()
                .collect(Collectors.toMap(AttendanceRecord::getAttendanceDate, r -> r));

        List<DayScheduleDTO> result = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            AttendanceRecord record = recordMap.get(date);
            DayScheduleDTO dto = createDayScheduleDTO(employee.getId(), date, record);
            result.add(dto);
            date = date.plusDays(1);
        }

        return result;
    }

    private DayScheduleDTO createDayScheduleDTO(Long employeeId, LocalDate date, AttendanceRecord record) {
        DayScheduleDTO dto = new DayScheduleDTO();
        dto.setDate(date.toString());
        dto.setDayName(date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID")));

        if (record != null) {
            // Has attendance record
            dto.setHasAttendance(true);
            dto.setClockInTime(record.getClockInTime() != null ? record.getClockInTime().toString() : null);
            dto.setClockOutTime(record.getClockOutTime() != null ? record.getClockOutTime().toString() : null);
            dto.setIsLate(record.getIsLate());
            dto.setLateDurationMinutes(record.getLateDurationMinutes());
            dto.setIsEarlyLeave(record.getIsEarlyLeave());
            dto.setEarlyLeaveDurationMinutes(record.getEarlyLeaveDurationMinutes());
            dto.setIsOvertime(record.getIsOvertime());
            dto.setOvertimeDurationMinutes(record.getOvertimeDurationMinutes());
            dto.setActualWorkMinutes(record.getActualWorkMinutes());
            dto.setIsWfh(record.getIsWfh());
            dto.setStatus(record.getStatus() != null ? record.getStatus().getDisplayName() : null);
            dto.setStatusCode(record.getStatus() != null ? record.getStatus().name() : null);

            if (record.getShiftPattern() != null) {
                dto.setShiftName(record.getShiftPattern().getName());
                dto.setShiftColor(record.getShiftPattern().getColor());
            }
            if (record.getWorkingHours() != null) {
                dto.setShiftStartTime(record.getWorkingHours().getStartTime() != null ? record.getWorkingHours().getStartTime().toString() : null);
                dto.setShiftEndTime(record.getWorkingHours().getEndTime() != null ? record.getWorkingHours().getEndTime().toString() : null);
            }
        } else {
            // No attendance record - get shift schedule
            EmployeeShiftService.ShiftAssignmentResult shift = employeeShiftService.getShiftAssignment(employeeId, date);
            dto.setHasAttendance(false);
            dto.setIsWorkingDay(shift.isWorkingDay());

            // Check holiday
            Holiday holiday = holidayService.getHolidayByDate(date).orElse(null);
            if (holiday != null) {
                dto.setIsHoliday(true);
                dto.setHolidayName(holiday.getName());
            }

            if (shift.getShiftPattern() != null) {
                dto.setShiftName(shift.getShiftPattern().getName());
                dto.setShiftColor(shift.getShiftPattern().getColor());
            }
            if (shift.getStartTime() != null) {
                dto.setShiftStartTime(shift.getStartTime().toString());
            }
            if (shift.getEndTime() != null) {
                dto.setShiftEndTime(shift.getEndTime().toString());
            }
        }

        return dto;
    }

    // =====================================================
    // DTOs
    // =====================================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayScheduleDTO {
        private String date;
        private String dayName;
        private Boolean hasAttendance;
        private Boolean isWorkingDay;
        private Boolean isHoliday;
        private String holidayName;

        // Shift info
        private String shiftName;
        private String shiftColor;
        private String shiftStartTime;
        private String shiftEndTime;

        // Attendance info
        private String clockInTime;
        private String clockOutTime;
        private Boolean isLate;
        private Integer lateDurationMinutes;
        private Boolean isEarlyLeave;
        private Integer earlyLeaveDurationMinutes;
        private Boolean isOvertime;
        private Integer overtimeDurationMinutes;
        private Integer actualWorkMinutes;
        private Boolean isWfh;
        private String status;
        private String statusCode;
    }
}
