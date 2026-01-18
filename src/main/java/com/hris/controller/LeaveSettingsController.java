package com.hris.controller;

import com.hris.model.Employee;
import com.hris.model.LeaveBalance;
import com.hris.service.DepartmentService;
import com.hris.service.EmployeeService;
import com.hris.service.LeaveBalanceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Leave Settings Controller
 * Handles leave balance management for admin/HR
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/leave-settings")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class LeaveSettingsController {

    private final LeaveBalanceService leaveBalanceService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @GetMapping
    public String leaveSettings(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search,
            Model model) {
        log.info("Loading leave settings page - year: {}, departmentId: {}, search: {}", year, departmentId, search);

        List<LeaveBalance> balances;

        if (search != null && !search.isBlank()) {
            // Search by employee name
            balances = leaveBalanceService.searchLeaveBalancesByName(search, year);
            log.info("Found {} leave balances for search: {}", balances.size(), search);
        } else if (departmentId != null) {
            // Filter by department
            balances = leaveBalanceService.getLeaveBalancesByDepartment(departmentId, year);
            log.info("Found {} leave balances for department: {}", balances.size(), departmentId);
        } else {
            // Get all balances for the year
            balances = leaveBalanceService.getAllLeaveBalancesForYear(year);
            log.info("Found {} leave balances for year: {}", balances.size(), year);
        }

        model.addAttribute("activePage", "leave-settings");
        model.addAttribute("balances", balances);
        model.addAttribute("year", year);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("search", search);

        // Get current year for default
        model.addAttribute("currentYear", LocalDate.now().getYear());

        // Get departments for filter
        model.addAttribute("departments", departmentService.getAllDepartments());

        // Stats
        long totalEmployees = balances.size();
        long withSufficientBalance = balances.stream()
                .filter(b -> b.getTotalAvailableBalance() >= 6)
                .count();
        long lowBalance = balances.stream()
                .filter(b -> b.getTotalAvailableBalance() > 0 && b.getTotalAvailableBalance() < 6)
                .count();
        long zeroBalance = balances.stream()
                .filter(b -> b.getTotalAvailableBalance() <= 0)
                .count();

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("withSufficientBalance", withSufficientBalance);
        model.addAttribute("lowBalance", lowBalance);
        model.addAttribute("zeroBalance", zeroBalance);

        return "leave-settings/list";
    }

    @PostMapping("/bulk-init")
    public String bulkInitializeLeaveBalance(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(required = false) Long departmentId,
            RedirectAttributes redirectAttributes) {
        log.info("Bulk initializing leave balance for year: {}, department: {}", year, departmentId);

        try {
            List<Employee> employees;
            if (departmentId != null) {
                employees = employeeService.getEmployeesByDepartment(departmentId);
            } else {
                employees = employeeService.getAllEmployees();
            }

            int successCount = 0;
            int skipCount = 0;

            for (Employee employee : employees) {
                try {
                    leaveBalanceService.initializeLeaveBalance(employee.getId(), year);
                    successCount++;
                } catch (Exception e) {
                    log.warn("Could not initialize leave balance for employee {}: {}", employee.getId(), e.getMessage());
                    skipCount++;
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    String.format("Berhasil menginisialisasi %d karyawan (dilewati: %d)", successCount, skipCount));
        } catch (Exception e) {
            log.error("Error bulk initializing leave balance", e);
            redirectAttributes.addFlashAttribute("error",
                    "Gagal bulk inisialisasi: " + e.getMessage());
        }

        return "redirect:/leave-settings?year=" + year;
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("Loading add leave balance form");

        model.addAttribute("activePage", "leave-settings");
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("currentYear", LocalDate.now().getYear());

        return "leave-settings/add";
    }

    @GetMapping("/edit/{employeeId}/{year}")
    public String editLeaveBalance(@PathVariable Long employeeId, @PathVariable int year, Model model) {
        log.info("Loading edit leave balance form - employeeId: {}, year: {}", employeeId, year);

        Employee employee = employeeService.getEmployeeById(employeeId);
        LeaveBalance balance = leaveBalanceService.getLeaveBalance(employeeId, year)
                .orElseGet(() -> {
                    LeaveBalance newBalance = new LeaveBalance();
                    newBalance.setEmployee(employee);
                    newBalance.setYear(year);
                    newBalance.setAnnualQuota(12);
                    return newBalance;
                });

        model.addAttribute("activePage", "leave-settings");
        model.addAttribute("balance", balance);
        model.addAttribute("employee", employee);
        model.addAttribute("isEdit", true);

        return "leave-settings/form";
    }

    @PostMapping("/save")
    public String saveLeaveBalance(
            @ModelAttribute LeaveBalance balance,
            @RequestParam(required = false) Long employeeId,
            @RequestParam int year,
            RedirectAttributes redirectAttributes) {
        log.info("Saving leave balance - employeeId: {}, year: {}, quota: {}, carriedForward: {}",
                employeeId, year, balance.getAnnualQuota(), balance.getCarriedForward());

        try {
            if (employeeId != null) {
                Employee employee = employeeService.getEmployeeById(employeeId);
                balance.setEmployee(employee);
            }

            balance.setYear(year);

            // Calculate balance if not set
            if (balance.getBalance() == null) {
                double totalAvailable = balance.getAnnualQuota() +
                        (balance.getCarriedForward() != null ? balance.getCarriedForward() : 0);
                balance.setBalance(totalAvailable);
            }

            leaveBalanceService.saveLeaveBalance(balance);

            redirectAttributes.addFlashAttribute("success",
                    "Leave balance berhasil disimpan untuk " + balance.getEmployee().getFullName());
        } catch (Exception e) {
            log.error("Error saving leave balance", e);
            redirectAttributes.addFlashAttribute("error",
                    "Gagal menyimpan leave balance: " + e.getMessage());
            return "redirect:/leave-settings/edit/" + employeeId + "/" + year;
        }

        return "redirect:/leave-settings?year=" + year;
    }

    @PostMapping("/initialize/{employeeId}")
    public String initializeLeaveBalance(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "2026") int year,
            RedirectAttributes redirectAttributes) {
        log.info("Initializing leave balance - employeeId: {}, year: {}", employeeId, year);

        try {
            leaveBalanceService.initializeLeaveBalance(employeeId, year);
            redirectAttributes.addFlashAttribute("success", "Leave balance berhasil diinisialisasi");
        } catch (Exception e) {
            log.error("Error initializing leave balance", e);
            redirectAttributes.addFlashAttribute("error",
                    "Gagal menginisialisasi leave balance: " + e.getMessage());
        }

        return "redirect:/leave-settings?year=" + year;
    }

    @PostMapping("/reset/{employeeId}")
    public String resetLeaveBalance(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "2026") int year,
            RedirectAttributes redirectAttributes) {
        log.info("Resetting leave balance for new year - employeeId: {}, year: {}", employeeId, year);

        try {
            leaveBalanceService.resetLeaveBalanceForNewYear(employeeId, year - 1);
            redirectAttributes.addFlashAttribute("success", "Leave balance berhasil di-reset untuk tahun baru");
        } catch (Exception e) {
            log.error("Error resetting leave balance", e);
            redirectAttributes.addFlashAttribute("error",
                    "Gagal me-reset leave balance: " + e.getMessage());
        }

        return "redirect:/leave-settings?year=" + year;
    }
}
