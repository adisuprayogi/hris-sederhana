package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.LeaveRequest;
import com.hris.model.enums.LeaveRequestStatus;
import com.hris.model.enums.LeaveType;
import com.hris.repository.EmployeeRepository;
import com.hris.service.LeaveRequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

/**
 * Controller for Leave Request pages
 * Handles leave request submission, approval, and management
 */
@Controller
@RequestMapping("/leave")
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeRepository employeeRepository;

    // =====================================================
    // MAIN LEAVE PAGES
    // =====================================================

    /**
     * Main leave page - redirects to my requests
     */
    @GetMapping
    public String leaveIndex() {
        return "redirect:/leave/my-requests";
    }

    /**
     * New leave request page
     */
    @GetMapping("/new")
    public String newLeaveRequest(Model model) {
        model.addAttribute("leaveTypes", LeaveType.values());
        return "leave/new";
    }

    /**
     * My leave requests page
     */
    @GetMapping("/my-requests")
    public String myRequests(Principal principal, Model model) {
        Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                .orElse(null);
        if (employee == null) {
            return "redirect:/login";
        }

        model.addAttribute("requests",
                leaveRequestService.getLeaveRequestsByEmployee(employee.getId()));
        model.addAttribute("leaveTypes", LeaveType.values());
        return "leave/my-requests";
    }

    // =====================================================
    // APPROVAL PAGES
    // =====================================================

    /**
     * Pending supervisor approval page
     */
    @GetMapping("/pending-supervisor")
    public String pendingSupervisor(Model model) {
        model.addAttribute("requests", leaveRequestService.getPendingSupervisorRequests());
        return "leave/pending-supervisor";
    }

    /**
     * Pending HR approval page
     */
    @GetMapping("/pending-hr")
    public String pendingHr(Model model) {
        model.addAttribute("requests", leaveRequestService.getPendingHrRequests());
        return "leave/pending-hr";
    }

    // =====================================================
    // SUBMIT LEAVE REQUEST
    // =====================================================

    /**
     * Submit new leave request
     */
    @PostMapping("/submit")
    public String submitLeaveRequest(
            Principal principal,
            @RequestParam LeaveType leaveType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElse(null);
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Employee not found");
                return "redirect:/leave/my-requests";
            }

            LeaveRequest leaveRequest = LeaveRequest.builder()
                    .employee(employee)
                    .leaveType(leaveType)
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason(reason)
                    .build();

            leaveRequestService.createLeaveRequest(leaveRequest);
            redirectAttributes.addFlashAttribute("success", "Pengajuan cuti berhasil dikirim");
        } catch (Exception e) {
            log.error("Error submitting leave request", e);
            redirectAttributes.addFlashAttribute("error", "Gagal mengajukan cuti: " + e.getMessage());
        }
        return "redirect:/leave/my-requests";
    }

    // =====================================================
    // SUPERVISOR APPROVAL ACTIONS
    // =====================================================

    /**
     * Approve as supervisor
     */
    @PostMapping("/{id}/approve-supervisor")
    @ResponseBody
    public String approveBySupervisor(
            @PathVariable Long id,
            Principal principal,
            @RequestParam(required = false) String note) {
        try {
            Employee supervisor = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElse(null);
            if (supervisor == null) {
                return "Supervisor not found";
            }

            leaveRequestService.approveBySupervisor(id, supervisor.getId(), note);
            return "OK";
        } catch (Exception e) {
            log.error("Error approving leave request by supervisor", e);
            return e.getMessage();
        }
    }

    /**
     * Reject as supervisor
     */
    @PostMapping("/{id}/reject-supervisor")
    @ResponseBody
    public String rejectBySupervisor(
            @PathVariable Long id,
            Principal principal,
            @RequestParam String reason) {
        try {
            Employee supervisor = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElse(null);
            if (supervisor == null) {
                return "Supervisor not found";
            }

            leaveRequestService.rejectBySupervisor(id, supervisor.getId(), reason);
            return "OK";
        } catch (Exception e) {
            log.error("Error rejecting leave request by supervisor", e);
            return e.getMessage();
        }
    }

    // =====================================================
    // HR APPROVAL ACTIONS
    // =====================================================

    /**
     * Approve as HR
     */
    @PostMapping("/{id}/approve-hr")
    @ResponseBody
    public String approveByHr(
            @PathVariable Long id,
            Principal principal,
            @RequestParam(required = false) String note) {
        try {
            Employee hr = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElse(null);
            if (hr == null) {
                return "HR not found";
            }

            leaveRequestService.approveByHr(id, hr.getId(), note);
            return "OK";
        } catch (Exception e) {
            log.error("Error approving leave request by HR", e);
            return e.getMessage();
        }
    }

    /**
     * Reject as HR
     */
    @PostMapping("/{id}/reject-hr")
    @ResponseBody
    public String rejectByHr(
            @PathVariable Long id,
            Principal principal,
            @RequestParam String reason) {
        try {
            Employee hr = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElse(null);
            if (hr == null) {
                return "HR not found";
            }

            leaveRequestService.rejectByHr(id, hr.getId(), reason);
            return "OK";
        } catch (Exception e) {
            log.error("Error rejecting leave request by HR", e);
            return e.getMessage();
        }
    }

    // =====================================================
    // CANCEL ACTIONS
    // =====================================================

    /**
     * Cancel leave request
     */
    @PostMapping("/{id}/cancel")
    public String cancelLeaveRequest(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeRepository.findByEmailAndDeletedAtIsNull(principal.getName())
                    .orElse(null);
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Employee not found");
                return "redirect:/leave/my-requests";
            }

            leaveRequestService.cancelLeaveRequest(id, employee);
            redirectAttributes.addFlashAttribute("success", "Pengajuan cuti berhasil dibatalkan");
        } catch (Exception e) {
            log.error("Error cancelling leave request", e);
            redirectAttributes.addFlashAttribute("error", "Gagal membatalkan: " + e.getMessage());
        }
        return "redirect:/leave/my-requests";
    }
}
