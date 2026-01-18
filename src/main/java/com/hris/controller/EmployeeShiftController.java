package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.EmployeeShiftSchedule;
import com.hris.model.EmployeeShiftSetting;
import com.hris.model.ShiftPattern;
import com.hris.model.WorkingHours;
import com.hris.service.EmployeeService;
import com.hris.service.EmployeeShiftService;
import com.hris.service.ShiftPatternService;
import com.hris.service.WorkingHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
}
