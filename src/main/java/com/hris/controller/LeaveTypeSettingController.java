package com.hris.controller;

import com.hris.model.LeaveTypeSetting;
import com.hris.model.enums.GenderRestriction;
import com.hris.model.enums.LeaveTypeEnum;
import com.hris.service.LeaveTypeSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller untuk Leave Type Settings Management
 * Menangani operasi CRUD pengaturan jenis cuti
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/leave-types")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class LeaveTypeSettingController {

    private final LeaveTypeSettingService leaveTypeSettingService;

    /**
     * Prevent ID field from being bound from form data
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("id");
    }

    /**
     * List all leave types
     */
    @GetMapping
    public String listLeaveTypes(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) LeaveTypeEnum type,
            @RequestParam(required = false) Boolean active,
            Model model) {

        // Default to current year if not specified
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        List<LeaveTypeSetting> leaveTypes = new ArrayList<>();
        LeaveTypeSettingService.LeaveTypeStats stats = leaveTypeSettingService.getStats(year);

        // Apply filters
        if (type != null) {
            // Filter by type
            if (type == LeaveTypeEnum.QUOTA) {
                leaveTypes = leaveTypeSettingService.getQuotaTypesByYear(year);
            } else if (type == LeaveTypeEnum.NO_QUOTA) {
                leaveTypes = leaveTypeSettingService.getActiveLeaveTypesByYear(year)
                        .stream()
                        .filter(lt -> lt.getLeaveType() == LeaveTypeEnum.NO_QUOTA)
                        .toList();
            }
            model.addAttribute("currentType", type);
        } else {
            // Get all types for the year
            leaveTypes = leaveTypeSettingService.getAllLeaveTypesByYear(year);
        }

        // Ensure leaveTypes is never null
        if (leaveTypes == null) {
            leaveTypes = new ArrayList<>();
        }

        // Apply active filter if specified
        if (active != null) {
            final Boolean activeFilter = active;
            leaveTypes = leaveTypes.stream()
                    .filter(lt -> lt != null && activeFilter.equals(lt.getIsActive()))
                    .toList();
            model.addAttribute("activeOnly", active);
        }

        model.addAttribute("activePage", "leave-types");
        // Always add a non-null list to the model
        if (leaveTypes == null) {
            leaveTypes = new ArrayList<>();
        }
        model.addAttribute("leaveTypes", leaveTypes);
        model.addAttribute("tableRowsHtml", generateTableRowsHtml(leaveTypes));
        log.debug("Added leaveTypes to model: size={}, isEmpty={}", leaveTypes.size(), leaveTypes.isEmpty());
        model.addAttribute("year", year);
        model.addAttribute("stats", stats);
        model.addAttribute("leaveTypeEnums", LeaveTypeEnum.values());
        model.addAttribute("genderRestrictions", GenderRestriction.values());
        model.addAttribute("years", generateYearOptions());

        return "leave-types/list";
    }

    /**
     * Show add form
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        LeaveTypeSetting leaveTypeForm = new LeaveTypeSetting();
        leaveTypeForm.setYear(LocalDate.now().getYear());
        leaveTypeForm.setLeaveType(LeaveTypeEnum.QUOTA);
        leaveTypeForm.setAnnualQuota(12);
        leaveTypeForm.setAllowCarryForward(false);
        leaveTypeForm.setMaxCarryForwardDays(6);
        leaveTypeForm.setCarryForwardExpiryMonth(3);
        leaveTypeForm.setCarryForwardExpiryDay(31);
        leaveTypeForm.setMinYearsOfService(0);
        leaveTypeForm.setGenderRestriction(GenderRestriction.ALL);
        leaveTypeForm.setIsPaid(true);
        leaveTypeForm.setRequireProof(false);
        leaveTypeForm.setIsActive(true);

        model.addAttribute("activePage", "leave-types");
        model.addAttribute("leaveTypeForm", leaveTypeForm);
        model.addAttribute("leaveTypeEnums", LeaveTypeEnum.values());
        model.addAttribute("genderRestrictions", GenderRestriction.values());
        model.addAttribute("edit", false);

        return "leave-types/form";
    }

    /**
     * Show edit form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        LeaveTypeSetting leaveTypeForm = leaveTypeSettingService.getLeaveTypeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found with id: " + id));

        model.addAttribute("activePage", "leave-types");
        model.addAttribute("leaveTypeForm", leaveTypeForm);
        model.addAttribute("leaveTypeEnums", LeaveTypeEnum.values());
        model.addAttribute("genderRestrictions", GenderRestriction.values());
        model.addAttribute("edit", true);

        return "leave-types/form";
    }

    /**
     * Save new leave type
     */
    @PostMapping("/save")
    public String saveLeaveType(
            @Valid @ModelAttribute("leaveTypeForm") LeaveTypeSetting leaveTypeForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("leaveTypeEnums", LeaveTypeEnum.values());
            model.addAttribute("genderRestrictions", GenderRestriction.values());
            model.addAttribute("edit", false);
            return "leave-types/form";
        }

        try {
            LeaveTypeSetting saved = leaveTypeSettingService.createLeaveType(leaveTypeForm);
            redirectAttributes.addFlashAttribute("success",
                    "Jenis cuti '" + saved.getName() + "' berhasil ditambahkan");
            return "redirect:/leave-types?year=" + saved.getYear();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("leaveTypeEnums", LeaveTypeEnum.values());
            model.addAttribute("genderRestrictions", GenderRestriction.values());
            model.addAttribute("edit", false);
            return "leave-types/form";
        }
    }

    /**
     * Update leave type
     */
    @PostMapping("/update/{id}")
    public String updateLeaveType(
            @PathVariable("id") Long leaveTypeId,
            @Valid @ModelAttribute("leaveTypeForm") LeaveTypeSetting leaveTypeForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("leaveTypeEnums", LeaveTypeEnum.values());
            model.addAttribute("genderRestrictions", GenderRestriction.values());
            model.addAttribute("edit", true);
            return "leave-types/form";
        }

        try {
            LeaveTypeSetting updated = leaveTypeSettingService.updateLeaveType(leaveTypeId, leaveTypeForm);
            redirectAttributes.addFlashAttribute("success",
                    "Jenis cuti '" + updated.getName() + "' berhasil diupdate");
            return "redirect:/leave-types?year=" + updated.getYear();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("leaveTypeEnums", LeaveTypeEnum.values());
            model.addAttribute("genderRestrictions", GenderRestriction.values());
            model.addAttribute("edit", true);
            return "leave-types/form";
        }
    }

    /**
     * Delete leave type
     */
    @PostMapping("/delete/{id}")
    public String deleteLeaveType(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            LeaveTypeSetting leaveType = leaveTypeSettingService.getLeaveTypeById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Leave type not found with id: " + id));
            leaveTypeSettingService.deleteLeaveType(id);
            redirectAttributes.addFlashAttribute("success",
                    "Jenis cuti '" + leaveType.getName() + "' berhasil dihapus");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/leave-types";
    }

    /**
     * Toggle active status
     */
    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            LeaveTypeSetting updated = leaveTypeSettingService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("success",
                    "Jenis cuti '" + updated.getName() + "' sekarang " +
                            (updated.getIsActive() ? "AKTIF" : "NON-AKTIF"));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/leave-types?year=" +
                leaveTypeSettingService.getLeaveTypeById(id).get().getYear();
    }

    /**
     * Generate year options for filter dropdown
     */
    private List<Integer> generateYearOptions() {
        int currentYear = LocalDate.now().getYear();
        return List.of(currentYear - 2, currentYear - 1, currentYear, currentYear + 1, currentYear + 2);
    }

    /**
     * Generate HTML table rows for leave types
     * Workaround for Thymeleaf th:each parsing issue with layout:decorate
     */
    private String generateTableRowsHtml(List<LeaveTypeSetting> leaveTypes) {
        if (leaveTypes == null || leaveTypes.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        for (LeaveTypeSetting lt : leaveTypes) {
            html.append("<tr class=\"hover:bg-gray-50 transition-colors\">");
            html.append("<td class=\"px-6 py-4\">");
            html.append("<span class=\"inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800\">")
                .append(escapeHtml(lt.getCode())).append("</span>");
            html.append("</td>");
            html.append("<td class=\"px-6 py-4\">");
            html.append("<div>");
            html.append("<div class=\"text-sm font-medium text-gray-900\">").append(escapeHtml(lt.getName())).append("</div>");
            html.append("<div class=\"text-xs text-gray-500\">").append(escapeHtml(lt.getDescription() != null ? lt.getDescription() : "")).append("</div>");
            html.append("</div>");
            html.append("</td>");
            html.append("<td class=\"px-6 py-4\">");
            String typeClass = lt.getLeaveType() == LeaveTypeEnum.QUOTA ? "bg-purple-100 text-purple-800" : "bg-gray-100 text-gray-800";
            html.append("<span class=\"inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ").append(typeClass).append("\">");
            html.append(lt.getLeaveType().getDisplayName()).append("</span>");
            html.append("</td>");
            html.append("<td class=\"px-6 py-4 text-right\">");
            html.append("<span class=\"text-sm font-medium text-gray-900\">").append(lt.getQuotaDisplay()).append("</span>");
            html.append("</td>");
            html.append("<td class=\"px-6 py-4 text-center\">");
            if (lt.hasCarryForward()) {
                html.append("<div>");
                html.append("<div class=\"text-xs font-medium text-green-600\">").append(lt.getMaxCarryForwardDays()).append(" hari</div>");
                html.append("<div class=\"text-xs text-gray-400\">Exp: ").append(lt.getCarryForwardExpiryFormatted()).append("</div>");
                html.append("</div>");
            } else {
                html.append("<span class=\"text-xs text-gray-400\">-</span>");
            }
            html.append("</td>");
            html.append("<td class=\"px-6 py-4\">");
            html.append("<div class=\"text-xs text-gray-600\">");
            html.append("<div>Masa kerja: <span class=\"font-medium\">").append(lt.getMinYearsDisplay()).append("</span></div>");
            html.append("<div>Gender: <span class=\"font-medium\">").append(lt.getGenderRestrictionDisplay()).append("</span></div>");
            if (lt.getRequireProof()) {
                html.append("<div><span class=\"text-orange-600\">• Perlu bukti</span></div>");
            }
            html.append("</div>");
            html.append("</td>");
            html.append("<td class=\"px-6 py-4 text-center\">");
            html.append("<form action=\"/leave-types/toggle/").append(lt.getId()).append("\" method=\"post\" class=\"inline\">");
            String statusClass = lt.getIsActive() ? "bg-green-100 text-green-800 hover:bg-green-200" : "bg-red-100 text-red-800 hover:bg-red-200";
            String statusText = lt.getIsActive() ? "Aktif" : "Non-Aktif";
            html.append("<button type=\"submit\" class=\"px-2.5 py-0.5 rounded-full text-xs font-medium transition-colors ").append(statusClass).append("\">");
            html.append("<span>").append(statusText).append("</span>");
            html.append("</button>");
            html.append("</form>");
            html.append("</td>");
            html.append("<td class=\"px-6 py-4 text-center\">");
            html.append("<div class=\"flex items-center justify-center gap-2\">");
            html.append("<a href=\"/leave-types/edit/").append(lt.getId()).append("\"");
            html.append(" class=\"inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-lg bg-blue-50 text-blue-700 hover:bg-blue-100 transition-colors\">");
            html.append("<svg class=\"w-3.5 h-3.5 mr-1\" xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke-width=\"1.5\" stroke=\"currentColor\">");
            html.append("<path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L6.832 19.82a4.5 4.5 0 01-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 011.13-1.897L16.863 4.487zm0 0L19.5 7.125\" />");
            html.append("</svg>Edit</a>");
            html.append("<form action=\"/leave-types/delete/").append(lt.getId()).append("\" method=\"post\" class=\"inline\" onsubmit=\"return confirm('Yakin ingin menghapus jenis cuti ini?')\">");
            html.append("<button type=\"submit\" class=\"inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-lg bg-red-50 text-red-700 hover:bg-red-100 transition-colors\">");
            html.append("<svg class=\"w-3.5 h-3.5 mr-1\" xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke-width=\"1.5\" stroke=\"currentColor\">");
            html.append("<path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0\" />");
            html.append("</svg>Hapus</button>");
            html.append("</form>");
            html.append("</div>");
            html.append("</td>");
            html.append("</tr>");
        }
        return html.toString();
    }

    /**
     * Simple HTML escape method
     */
    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
