package com.hris.controller;

import com.hris.dto.BulkAssignShiftRequest;
import com.hris.dto.BulkAssignShiftResult;
import com.hris.model.Employee;
import com.hris.model.EmployeeShiftSchedule;
import com.hris.model.EmployeeShiftSetting;
import com.hris.model.ShiftPattern;
import com.hris.model.WorkingHours;
import com.hris.model.enums.EmployeeStatus;
import com.hris.service.*;
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
import java.util.List;

/**
 * Employee Shift Controller
 * Manage shift assignment and override schedules for employees
 */
@Slf4j
@Controller
@RequestMapping("/employees")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@RequiredArgsConstructor
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;
    private final EmployeeService employeeService;
    private final ShiftPatternService shiftPatternService;
    private final WorkingHoursService workingHoursService;
    private final DepartmentService departmentService;
    private final CompanyService companyService;

    // =====================================================
    // EMPLOYEE SHIFT ASSIGNMENT
    // =====================================================

    /**
     * Show shift settings for an employee
     */
    @GetMapping("/{id}/shift-settings")
    public String showShiftSettings(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee tidak ditemukan");
            return "redirect:/employees";
        }

        List<EmployeeShiftSetting> settings = employeeShiftService.getEmployeeShiftSettings(id);
        List<ShiftPattern> shiftPatterns = shiftPatternService.getAllShiftPatternsWithShiftPackage();

        model.addAttribute("employee", employee);
        model.addAttribute("settings", settings);
        model.addAttribute("shiftPatterns", shiftPatterns);
        model.addAttribute("activePage", "employees");
        return "employee/shift-settings";
    }

    /**
     * Assign shift pattern to employee
     */
    @PostMapping("/{id}/shift-assign")
    public String assignShiftPattern(@PathVariable Long id,
                                      @RequestParam Long shiftPatternId,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
                                      @RequestParam(required = false) String reason,
                                      @RequestParam(required = false) String notes,
                                      RedirectAttributes redirectAttributes) {
        try {
            employeeShiftService.assignShiftPattern(id, shiftPatternId, effectiveFrom, reason, notes, null);
            redirectAttributes.addFlashAttribute("success", "Shift pattern berhasil di-assign");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employees/{id}/shift-settings";
    }

    // =====================================================
    // EMPLOYEE SHIFT SCHEDULE (OVERRIDE)
    // =====================================================

    /**
     * Show shift schedules for an employee
     */
    @GetMapping("/{id}/shift-schedules")
    public String showShiftSchedules(@PathVariable Long id,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee tidak ditemukan");
            return "redirect:/employees";
        }

        // Default to current month if no date range specified
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }

        List<EmployeeShiftSchedule> schedules = employeeShiftService.getOverrideSchedules(id, startDate, endDate);
        List<WorkingHours> workingHoursList = workingHoursService.getAllWorkingHours();

        model.addAttribute("employee", employee);
        model.addAttribute("schedules", schedules);
        model.addAttribute("workingHoursList", workingHoursList);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activePage", "employees");
        return "employee/shift-schedules";
    }

    /**
     * Create override schedule
     */
    @PostMapping("/{id}/shift-schedules/save")
    public String createOverrideSchedule(@PathVariable Long id,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleDate,
                                         @RequestParam(required = false) Long workingHoursId,
                                         @RequestParam(required = false) Boolean overrideIsWfh,
                                         @RequestParam(required = false) Boolean overrideIsOvertimeAllowed,
                                         @RequestParam(required = false) Boolean overrideAttendanceMandatory,
                                         @RequestParam(required = false) String notes,
                                         RedirectAttributes redirectAttributes) {
        try {
            employeeShiftService.createOverrideSchedule(id, scheduleDate, workingHoursId,
                    overrideIsWfh, overrideIsOvertimeAllowed, overrideAttendanceMandatory, notes, null);
            redirectAttributes.addFlashAttribute("success", "Override schedule berhasil dibuat");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employees/{id}/shift-schedules";
    }

    /**
     * Delete override schedule
     */
    @PostMapping("/{id}/shift-schedules/{scheduleId}/delete")
    public String deleteOverrideSchedule(@PathVariable Long id,
                                         @PathVariable Long scheduleId,
                                         RedirectAttributes redirectAttributes) {
        try {
            employeeShiftService.deleteOverrideSchedule(scheduleId);
            redirectAttributes.addFlashAttribute("success", "Override schedule berhasil dihapus");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employees/{id}/shift-schedules";
    }

    /**
     * Get shift schedule for detail modal (AJAX)
     * Returns weekly breakdown of shift assignments
     */
    @GetMapping("/{id}/shift-schedule")
    @ResponseBody
    public List<WeekScheduleDTO> getShiftSchedule(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Getting shift schedule for employee {} from {} to {}", id, startDate, endDate);
        return employeeShiftService.getShiftScheduleByWeeks(id, startDate, endDate);
    }

    /**
     * Show shift detail page for employee
     */
    @GetMapping("/{id}/shift-detail")
    public String showShiftDetail(@PathVariable Long id,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Integer month,
                                  @RequestParam(required = false) Integer year,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee tidak ditemukan");
            return "redirect:/employees";
        }

        // Default to current month/year if not specified
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        List<ShiftPattern> shiftPatterns = shiftPatternService.getAllShiftPatternsWithShiftPackage();

        // Get payroll cutoff date from company settings
        Integer payrollCutoffDate = 25; // default
        var company = companyService.getCompany();
        if (company != null && company.getEmployeePayrollCutoffDate() != null) {
            payrollCutoffDate = company.getEmployeePayrollCutoffDate();
        }

        model.addAttribute("employee", employee);
        model.addAttribute("shiftPatterns", shiftPatterns);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("payrollCutoffDate", payrollCutoffDate);
        model.addAttribute("activePage", "employees");
        return "employee/shift-detail";
    }

    // =====================================================
    // BULK SHIFT ASSIGNMENT
    // =====================================================

    /**
     * Show bulk shift assignment page
     */
    @GetMapping("/shift-assign-bulk")
    public String showBulkAssignForm(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long shiftPatternId,
            @RequestParam(required = false) String status,
            Model model) {
        // Get all shift patterns for dropdown
        List<ShiftPattern> shiftPatterns = shiftPatternService.getAllShiftPatternsWithShiftPackage();
        model.addAttribute("shiftPatterns", shiftPatterns);

        // Get all departments for filter
        var departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);

        // Pre-fill filters
        model.addAttribute("filterDepartmentId", departmentId);
        model.addAttribute("filterShiftPatternId", shiftPatternId);
        model.addAttribute("filterStatus", status);

        model.addAttribute("activePage", "bulk-assign-shift");
        model.addAttribute("title", "Bulk Assign Shift Pattern");
        return "employee/bulk-assign";
    }

    /**
     * Get filtered employees for bulk assignment (AJAX)
     */
    @GetMapping("/shift-assign-bulk/employees")
    @ResponseBody
    public List<EmployeeShiftSummary> getFilteredEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long currentShiftPatternId) {
        log.info("Getting filtered employees - dept: {}, shift: {}", departmentId, currentShiftPatternId);

        // Get only active employees based on filters
        List<Employee> employees;
        if (departmentId != null) {
            employees = employeeService.getEmployeesByDepartment(departmentId).stream()
                    .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                    .toList();
        } else {
            employees = employeeService.getAllEmployees().stream()
                    .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                    .toList();
        }

        // Convert to summary DTO with current shift info
        return employees.stream()
                .map(emp -> {
                    // Get the active shift setting (not just pattern)
                    var activeSetting = employeeShiftService.getActiveShiftSetting(emp.getId(), LocalDate.now());
                    ShiftPattern currentShift = null;
                    String effectiveFrom = null;

                    if (activeSetting.isPresent()) {
                        EmployeeShiftSetting setting = activeSetting.get();
                        currentShift = shiftPatternService.getShiftPatternById(setting.getShiftPatternId());
                        effectiveFrom = setting.getEffectiveFrom() != null
                            ? setting.getEffectiveFrom().toString()
                            : null;
                    }

                    // Filter by current shift if specified
                    if (currentShiftPatternId != null && currentShift != null) {
                        if (!currentShift.getId().equals(currentShiftPatternId)) {
                            return null;
                        }
                    }
                    return new EmployeeShiftSummary(emp, currentShift, effectiveFrom);
                })
                .filter(item -> item != null)
                .toList();
    }

    /**
     * Execute bulk shift assignment
     */
    @PostMapping("/shift-assign-bulk/execute")
    @ResponseBody
    public BulkAssignShiftResult executeBulkAssign(
            @RequestBody BulkAssignShiftRequest request,
            Principal principal) {
        log.info("Executing bulk shift assignment for {} employees by user {}",
                request.getEmployeeIds().size(), principal.getName());

        try {
            return employeeShiftService.bulkAssignShiftPattern(request, null);
        } catch (Exception e) {
            log.error("Bulk assignment failed", e);
            BulkAssignShiftResult errorResult = BulkAssignShiftResult.builder()
                    .retroactive(request.isRetroactive())
                    .retroactiveDays(request.getRetroactiveDays())
                    .build();
            errorResult.getFailureList().add(BulkAssignShiftResult.FailureItem.builder()
                    .errorMessage("Bulk assignment failed: " + e.getMessage())
                    .errorType("SYSTEM_ERROR")
                    .build());
            return errorResult;
        }
    }

    /**
     * Summary DTO for employee with current shift
     */
    @Data
    @AllArgsConstructor
    public static class EmployeeShiftSummary {
        private Long id;
        private String fullName;
        private String email;
        private String departmentName;
        private String positionName;
        private String photoPath;
        private Long currentShiftPatternId;
        private String currentShiftPatternName;
        private String currentShiftPatternCode;
        private String currentShiftColor;
        private String currentShiftEffectiveFrom;

        public EmployeeShiftSummary(Employee employee, ShiftPattern currentShift, String effectiveFrom) {
            this.id = employee.getId();
            this.fullName = employee.getFullName();
            this.email = employee.getEmail();
            this.departmentName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
            this.positionName = employee.getPosition() != null ? employee.getPosition().getName() : null;
            this.photoPath = employee.getPhotoPath();
            this.currentShiftPatternId = currentShift != null ? currentShift.getId() : null;
            this.currentShiftPatternName = currentShift != null ? currentShift.getName() : null;
            this.currentShiftPatternCode = currentShift != null ? currentShift.getCode() : null;
            this.currentShiftColor = currentShift != null ? currentShift.getColor() : null;
            this.currentShiftEffectiveFrom = effectiveFrom;
        }
    }

    /**
     * Week Schedule DTO for shift detail modal
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeekScheduleDTO {
        private Integer weekNumber;
        private String dateRange;
        private List<DayScheduleDTO> days;
    }

    /**
     * Day Schedule DTO for shift detail modal
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayScheduleDTO {
        private String date;
        private String dayName;
        private Boolean isWorkingDay;
        private Boolean isHoliday;
        private Boolean isWeeklyLeave;
        private String shiftName;
        private String shiftColor;
        private String workingHours;
        private String holidayName;
    }
}
