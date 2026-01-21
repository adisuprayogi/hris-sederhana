package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.OvertimeRequest;
import com.hris.model.enums.RequestStatus;
import com.hris.repository.EmployeeRepository;
import com.hris.service.OvertimeRequestService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/**
 * Overtime Request Controller
 * Handles overtime request submission and approval
 */
@Slf4j
@Controller
@RequestMapping("/overtime")
@RequiredArgsConstructor
public class OvertimeRequestController {

    private final OvertimeRequestService overtimeRequestService;
    private final EmployeeRepository employeeRepository;

    // =====================================================
    // PAGES
    // =====================================================

    /**
     * Overtime request list page
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String myRequestsPage(Model model, Principal principal) {
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                .orElse(null);
        if (employee == null) {
            return "redirect:/login";
        }

        List<OvertimeRequest> requests = overtimeRequestService.getByEmployee(employee.getId());

        model.addAttribute("employee", employee);
        model.addAttribute("requests", requests);
        model.addAttribute("activePage", "overtime");
        return "overtime/my-requests";
    }

    /**
     * Overtime pending approval page (supervisor)
     */
    @GetMapping("/pending-supervisor")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String pendingSupervisorPage(Model model) {
        List<OvertimeRequest> requests = overtimeRequestService.getPendingSupervisorRequests();

        model.addAttribute("requests", requests);
        model.addAttribute("activePage", "overtime-approval");
        return "overtime/pending-supervisor";
    }

    /**
     * Overtime pending approval page (HR)
     */
    @GetMapping("/pending-hr")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String pendingHrPage(Model model) {
        List<OvertimeRequest> requests = overtimeRequestService.getPendingHrRequests();

        model.addAttribute("requests", requests);
        model.addAttribute("activePage", "overtime-approval");
        return "overtime/pending-hr";
    }

    // =====================================================
    // FORM SUBMISSION
    // =====================================================

    /**
     * Submit overtime request
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String submitRequest(
            @RequestParam @DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate requestDate,
            @RequestParam BigDecimal estimatedHours,
            @RequestParam String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            overtimeRequestService.submitRequest(employee.getId(), requestDate, estimatedHours, reason);
            redirectAttributes.addFlashAttribute("success", "Overtime request submitted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/overtime/my-requests";
    }

    // =====================================================
    // APPROVAL ACTIONS
    // =====================================================

    /**
     * Approve as supervisor
     */
    @PostMapping("/{id}/approve-supervisor")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String approveBySupervisor(
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Employee approver = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Approver not found"));
            overtimeRequestService.approveBySupervisor(id, approver.getId(), note);
            redirectAttributes.addFlashAttribute("success", "Overtime request approved");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/overtime/pending-supervisor";
    }

    /**
     * Reject as supervisor
     */
    @PostMapping("/{id}/reject-supervisor")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String rejectBySupervisor(
            @PathVariable Long id,
            @RequestParam String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Employee approver = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Approver not found"));
            overtimeRequestService.rejectBySupervisor(id, approver.getId(), reason);
            redirectAttributes.addFlashAttribute("success", "Overtime request rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/overtime/pending-supervisor";
    }

    /**
     * Approve as HR
     */
    @PostMapping("/{id}/approve-hr")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String approveByHr(
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Employee approver = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Approver not found"));
            overtimeRequestService.approveByHr(id, approver.getId(), note);
            redirectAttributes.addFlashAttribute("success", "Overtime request approved");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/overtime/pending-hr";
    }

    /**
     * Reject as HR
     */
    @PostMapping("/{id}/reject-hr")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String rejectByHr(
            @PathVariable Long id,
            @RequestParam String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Employee approver = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Approver not found"));
            overtimeRequestService.rejectByHr(id, approver.getId(), reason);
            redirectAttributes.addFlashAttribute("success", "Overtime request rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/overtime/pending-hr";
    }

    // =====================================================
    // DTO
    // =====================================================

    @Data
    public static class OvertimeRequestDTO {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private LocalDate requestDate;
        private BigDecimal estimatedHours;
        private Integer actualDurationMinutes;
        private String reason;
        private RequestStatus status;
        private String supervisorName;
        private String hrName;
        private String supervisorApprovalNote;
        private String hrApprovalNote;
    }
}
