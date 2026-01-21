package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.WfhRequest;
import com.hris.model.enums.RequestStatus;
import com.hris.repository.EmployeeRepository;
import com.hris.service.WfhRequestService;
import lombok.Data;
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
 * WFH Request Controller
 * Handles WFH request submission and approval
 */
@Slf4j
@Controller
@RequestMapping("/wfh")
@RequiredArgsConstructor
public class WfhRequestController {

    private final WfhRequestService wfhRequestService;
    private final EmployeeRepository employeeRepository;

    // =====================================================
    // PAGES
    // =====================================================

    /**
     * WFH request list page
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String myRequestsPage(Model model, Principal principal) {
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                .orElse(null);
        if (employee == null) {
            return "redirect:/login";
        }

        List<WfhRequest> requests = wfhRequestService.getByEmployee(employee.getId());

        model.addAttribute("employee", employee);
        model.addAttribute("requests", requests);
        model.addAttribute("activePage", "wfh");
        return "wfh/my-requests";
    }

    /**
     * WFH pending approval page (supervisor)
     */
    @GetMapping("/pending-supervisor")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String pendingSupervisorPage(Model model) {
        List<WfhRequest> requests = wfhRequestService.getPendingSupervisorRequests();

        model.addAttribute("requests", requests);
        model.addAttribute("activePage", "wfh-approval");
        return "wfh/pending-supervisor";
    }

    /**
     * WFH pending approval page (HR)
     */
    @GetMapping("/pending-hr")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String pendingHrPage(Model model) {
        List<WfhRequest> requests = wfhRequestService.getPendingHrRequests();

        model.addAttribute("requests", requests);
        model.addAttribute("activePage", "wfh-approval");
        return "wfh/pending-hr";
    }

    // =====================================================
    // FORM SUBMISSION
    // =====================================================

    /**
     * Submit WFH request
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public String submitRequest(
            @RequestParam @DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate requestDate,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            wfhRequestService.submitRequest(employee.getId(), requestDate, reason);
            redirectAttributes.addFlashAttribute("success", "WFH request submitted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/wfh/my-requests";
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
            wfhRequestService.approveBySupervisor(id, approver.getId(), note);
            redirectAttributes.addFlashAttribute("success", "WFH request approved");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/wfh/pending-supervisor";
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
            wfhRequestService.rejectBySupervisor(id, approver.getId(), reason);
            redirectAttributes.addFlashAttribute("success", "WFH request rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/wfh/pending-supervisor";
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
            wfhRequestService.approveByHr(id, approver.getId(), note);
            redirectAttributes.addFlashAttribute("success", "WFH request approved");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/wfh/pending-hr";
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
            wfhRequestService.rejectByHr(id, approver.getId(), reason);
            redirectAttributes.addFlashAttribute("success", "WFH request rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/wfh/pending-hr";
    }

    // =====================================================
    // DTO
    // =====================================================

    @Data
    public static class WfhRequestDTO {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private LocalDate requestDate;
        private String reason;
        private RequestStatus status;
        private String supervisorName;
        private String hrName;
        private String supervisorApprovalNote;
        private String hrApprovalNote;
    }
}
