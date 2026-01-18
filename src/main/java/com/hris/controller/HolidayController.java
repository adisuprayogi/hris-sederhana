package com.hris.controller;

import com.hris.model.Holiday;
import com.hris.model.enums.HolidayType;
import com.hris.service.HolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

/**
 * Controller untuk Holiday Management
 * Menangani operasi CRUD hari libur
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/holidays")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class HolidayController {

    private final HolidayService holidayService;

    /**
     * List all holidays
     */
    @GetMapping
    public String listHolidays(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) HolidayType type,
            @RequestParam(required = false) Boolean active,
            Model model) {

        List<Holiday> holidays;
        HolidayService.HolidayStats stats = holidayService.getHolidayStats();

        // Apply filters
        if (year != null && type != null) {
            // Both year and type filters
            holidays = holidayService.getHolidaysByYearAndType(year, type);
            model.addAttribute("currentYear", year);
            model.addAttribute("currentType", type);
        } else if (year != null) {
            // Only year filter
            holidays = holidayService.getHolidaysByYear(year);
            model.addAttribute("currentYear", year);
        } else if (type != null) {
            // Only type filter
            holidays = holidayService.getHolidaysByType(type);
            model.addAttribute("currentType", type);
        } else if (active != null) {
            // Only active status filter
            holidays = active ? holidayService.getActiveHolidays() : holidayService.getAllHolidays();
            model.addAttribute("activeOnly", active);
        } else {
            // No filters - show active holidays only
            holidays = holidayService.getActiveHolidays();
            model.addAttribute("currentYear", LocalDate.now().getYear());
        }

        // Apply active filter on top of other filters if specified
        if (active != null && year == null && type == null) {
            // Active filter only, already applied above
        } else if (active != null) {
            // Apply active filter on top of year/type filters
            holidays = holidays.stream()
                    .filter(h -> h.getIsActive() == active)
                    .toList();
            model.addAttribute("activeOnly", active);
        }

        model.addAttribute("holidays", holidays);
        model.addAttribute("stats", stats);
        model.addAttribute("holidayTypes", HolidayType.values());
        model.addAttribute("years", generateYearOptions());

        return "holidays/list";
    }

    /**
     * Show add holiday form
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("holiday", new Holiday());
        model.addAttribute("holidayTypes", HolidayType.values());
        model.addAttribute("edit", false);
        return "holidays/form";
    }

    /**
     * Show edit holiday form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Holiday holiday = holidayService.getHolidayById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with id: " + id));

        model.addAttribute("holiday", holiday);
        model.addAttribute("holidayTypes", HolidayType.values());
        model.addAttribute("edit", true);
        return "holidays/form";
    }

    /**
     * Save new holiday
     */
    @PostMapping("/save")
    public String saveHoliday(
            @Valid @ModelAttribute("holiday") Holiday holiday,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("holidayTypes", HolidayType.values());
            model.addAttribute("edit", false);
            return "holidays/form";
        }

        try {
            Holiday saved = holidayService.createHoliday(holiday);
            redirectAttributes.addFlashAttribute("success",
                    "Holiday '" + saved.getName() + "' created successfully");
            return "redirect:/holidays";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("holidayTypes", HolidayType.values());
            model.addAttribute("edit", false);
            return "holidays/form";
        }
    }

    /**
     * Update holiday
     */
    @PostMapping("/update/{id}")
    public String updateHoliday(
            @PathVariable Long id,
            @Valid @ModelAttribute("holiday") Holiday holiday,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("holidayTypes", HolidayType.values());
            model.addAttribute("edit", true);
            return "holidays/form";
        }

        try {
            Holiday updated = holidayService.updateHoliday(id, holiday);
            redirectAttributes.addFlashAttribute("success",
                    "Holiday '" + updated.getName() + "' updated successfully");
            return "redirect:/holidays";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("holidayTypes", HolidayType.values());
            model.addAttribute("edit", true);
            return "holidays/form";
        }
    }

    /**
     * Delete holiday
     */
    @PostMapping("/delete/{id}")
    public String deleteHoliday(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Holiday holiday = holidayService.getHolidayById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Holiday not found with id: " + id));
            holidayService.deleteHoliday(id);
            redirectAttributes.addFlashAttribute("success",
                    "Holiday '" + holiday.getName() + "' deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/holidays";
    }

    /**
     * Check if date is a holiday (API)
     */
    @GetMapping("/check")
    @ResponseBody
    public Map<String, Object> checkHoliday(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        boolean isHoliday = holidayService.isHoliday(date);
        Map<String, Object> result = Map.of(
                "date", date,
                "isHoliday", isHoliday
        );

        if (isHoliday) {
            Holiday holiday = holidayService.getHolidayByDate(date).orElse(null);
            if (holiday != null) {
                return Map.of(
                        "date", date,
                        "isHoliday", true,
                        "holidayName", holiday.getName(),
                        "holidayType", holiday.getHolidayType().name(),
                        "description", holiday.getDescription() != null ? holiday.getDescription() : ""
                );
            }
        }

        return result;
    }

    /**
     * Get upcoming holidays (API)
     */
    @GetMapping("/upcoming")
    @ResponseBody
    public List<Holiday> getUpcomingHolidays(@RequestParam(defaultValue = "30") int days) {
        return holidayService.getUpcomingHolidays(days);
    }

    /**
     * Toggle holiday active status
     */
    @PostMapping("/toggle/{id}")
    public String toggleHolidayStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Holiday holiday = holidayService.getHolidayById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with id: " + id));

        holiday.setIsActive(!holiday.getIsActive());
        holidayService.updateHoliday(id, holiday);

        redirectAttributes.addFlashAttribute("success",
                "Holiday '" + holiday.getName() + "' " +
                        (holiday.getIsActive() ? "activated" : "deactivated"));

        return "redirect:/holidays";
    }

    /**
     * Generate year options for filter dropdown
     */
    private List<Integer> generateYearOptions() {
        int currentYear = LocalDate.now().getYear();
        return List.of(currentYear - 1, currentYear, currentYear + 1, currentYear + 2);
    }
}
