package com.hris.controller;

import com.hris.model.ContractHistory;
import com.hris.model.Employee;
import com.hris.model.enums.EmploymentStatus;
import com.hris.model.enums.EmploymentStatusChange;
import com.hris.service.ContractHistoryService;
import com.hris.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller untuk Contract History
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/employees/{employeeId}/contract-history")
public class ContractHistoryController {

    private final ContractHistoryService contractHistoryService;
    private final EmployeeService employeeService;

    /**
     * Tampilkan riwayat kontrak dan status kerja karyawan
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String showContractHistory(@PathVariable Long employeeId, Model model) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        List<ContractHistory> contractHistory = contractHistoryService.getContractHistoryByEmployeeId(employeeId);
        Optional<ContractHistory> currentStatus = contractHistoryService.getCurrentEmploymentStatus(employeeId);

        // Get statistics
        long totalChanges = contractHistory.size();
        long contractPeriods = contractHistory.stream()
            .filter(ContractHistory::isContractPeriod)
            .count();
        boolean isPermanent = currentStatus.isPresent()
            && currentStatus.get().getNewStatus() == EmploymentStatus.PERMANENT;
        LocalDate permanentDate = isPermanent
            ? currentStatus.get().getPermanentAppointmentDate()
            : null;

        model.addAttribute("activePage", "employees");
        model.addAttribute("employee", employee);
        model.addAttribute("contractHistory", contractHistory);
        model.addAttribute("currentStatus", currentStatus.orElse(null));
        model.addAttribute("totalChanges", totalChanges);
        model.addAttribute("contractPeriods", contractPeriods);
        model.addAttribute("isPermanent", isPermanent);
        model.addAttribute("permanentDate", permanentDate);

        return "employee/contract-history";
    }

    /**
     * Form tambah perubahan status kerja
     */
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String showStatusChangeForm(@PathVariable Long employeeId, Model model) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        Optional<ContractHistory> currentStatus = contractHistoryService.getCurrentEmploymentStatus(employeeId);

        model.addAttribute("activePage", "employees");
        model.addAttribute("employee", employee);
        model.addAttribute("currentStatus", currentStatus.orElse(null));
        model.addAttribute("employmentStatuses", EmploymentStatus.values());
        model.addAttribute("employmentStatusChanges", EmploymentStatusChange.values());

        return "employee/contract-history-form";
    }

    /**
     * Upload dokumen untuk contract history
     */
    @PostMapping("/{id}/upload-document")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @ResponseBody
    public String uploadDocument(@PathVariable Long employeeId,
                                 @PathVariable Long id,
                                 @RequestParam("file") MultipartFile file) {
        try {
            String documentPath = contractHistoryService.uploadDocument(id, file);
            return documentPath;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to upload document: " + e.getMessage());
        }
    }

    /**
     * Proses perubahan status kerja
     */
    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String processStatusChange(
        @PathVariable Long employeeId,
        @RequestParam EmploymentStatusChange changeType,
        @RequestParam(required = false) EmploymentStatus newStatus,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String contractNumber,
        @RequestParam(required = false) String reason,
        @RequestParam(required = false) String notes,
        @RequestParam(required = false) MultipartFile document,
        Principal principal,
        RedirectAttributes redirectAttributes) {

        try {
            // Derive newStatus from changeType if not provided
            if (newStatus == null) {
                switch (changeType) {
                    case CONTRACT_TO_PERMANENT:
                    case PROBATION_TO_PERMANENT:
                        newStatus = EmploymentStatus.PERMANENT;
                        break;
                    case CONTRACT_RENEWAL:
                        newStatus = EmploymentStatus.CONTRACT;
                        break;
                    case PROBATION_TO_CONTRACT:
                        newStatus = EmploymentStatus.CONTRACT;
                        break;
                    default:
                        throw new IllegalArgumentException("Status baru harus dipilih");
                }
            }

            // Validate endDate for contract status
            if (newStatus == EmploymentStatus.CONTRACT && endDate == null) {
                redirectAttributes.addFlashAttribute("error",
                    "Tanggal berakhir harus diisi untuk status kontrak");
                return "redirect:/employees/" + employeeId + "/contract-history/new";
            }

            ContractHistory history;

            if (changeType == EmploymentStatusChange.CONTRACT_TO_PERMANENT
                || changeType == EmploymentStatusChange.PROBATION_TO_PERMANENT) {
                // Record permanent appointment (no end date)
                history = contractHistoryService.recordPermanentAppointment(employeeId, effectiveDate, reason);
            } else if (changeType == EmploymentStatusChange.CONTRACT_RENEWAL
                || changeType == EmploymentStatusChange.PROBATION_TO_CONTRACT) {
                // Record contract with end date
                history = contractHistoryService.recordContractRenewal(employeeId, effectiveDate, endDate, contractNumber, reason);
            } else {
                // General status change - check if contract status with end date
                if (newStatus == EmploymentStatus.CONTRACT) {
                    history = contractHistoryService.recordContractRenewal(employeeId, effectiveDate, endDate, contractNumber, reason);
                } else {
                    history = contractHistoryService.recordStatusChange(employeeId, changeType, newStatus, effectiveDate, reason, notes);
                }
            }

            // Upload document if provided
            if (document != null && !document.isEmpty()) {
                try {
                    contractHistoryService.uploadDocument(history.getId(), document);
                } catch (Exception e) {
                    // Log document upload error but don't fail the entire process
                    System.err.println("Document upload failed: " + e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("success",
                "Perubahan status kerja berhasil dicatat");

            return "redirect:/employees/" + employeeId + "/contract-history";

        } catch (IllegalStateException e) {
            // Handle specific validation errors
            String errorMsg = e.getMessage();
            if ("Cannot convert current status to permanent".equals(errorMsg)) {
                errorMsg = "Status karyawan saat ini tidak dapat diubah ke karyawan tetap. Pastikan status saat ini adalah Kontrak atau Masa Percobaan.";
            } else if ("No current employment status found".equals(errorMsg)) {
                errorMsg = "Riwayat status kerja karyawan tidak ditemukan. Silakan hubungi administrator.";
            } else if ("Employee already has employment status record".equals(errorMsg)) {
                errorMsg = "Data karyawan belum lengkap. Silakan hubungi administrator.";
            }
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/employees/" + employeeId + "/contract-history/new";
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging
            redirectAttributes.addFlashAttribute("error",
                "Terjadi kesalahan: " + e.getMessage());
            return "redirect:/employees/" + employeeId + "/contract-history/new";
        }
    }

    /**
     * Form edit riwayat kontrak
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String showEditForm(@PathVariable Long employeeId,
                               @PathVariable Long id,
                               Model model) {
        ContractHistory history = contractHistoryService.getContractHistoryById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contract history not found"));

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        model.addAttribute("activePage", "employees");
        model.addAttribute("employee", employee);
        model.addAttribute("history", history);
        model.addAttribute("employmentStatuses", EmploymentStatus.values());
        model.addAttribute("employmentStatusChanges", EmploymentStatusChange.values());

        return "employee/contract-history-form";
    }

    /**
     * Proses update riwayat kontrak
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String updateContractHistory(@PathVariable Long employeeId,
                                        @PathVariable Long id,
                                        @Valid @ModelAttribute ContractHistory updatedHistory,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Validasi gagal");
            return "redirect:/employees/" + employeeId + "/contract-history/" + id + "/edit";
        }

        try {
            contractHistoryService.updateContractHistory(id, updatedHistory);
            redirectAttributes.addFlashAttribute("success",
                "Riwayat kontrak berhasil diperbarui");
            return "redirect:/employees/" + employeeId + "/contract-history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Gagal memperbarui riwayat: " + e.getMessage());
            return "redirect:/employees/" + employeeId + "/contract-history/" + id + "/edit";
        }
    }

    /**
     * Hapus riwayat kontrak
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public String deleteContractHistory(@PathVariable Long employeeId,
                                        @PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        try {
            contractHistoryService.deleteContractHistory(id);
            redirectAttributes.addFlashAttribute("success",
                "Riwayat kontrak berhasil dihapus");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Gagal menghapus riwayat: " + e.getMessage());
        }
        return "redirect:/employees/" + employeeId + "/contract-history";
    }

    /**
     * API untuk mendapatkan riwayat kontrak karyawan (untuk AJAX)
     */
    @GetMapping("/api")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public List<ContractHistory> getContractHistoryApi(@PathVariable Long employeeId) {
        return contractHistoryService.getContractHistoryByEmployeeId(employeeId);
    }
}
