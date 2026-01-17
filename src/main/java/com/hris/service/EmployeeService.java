package com.hris.service;

import com.hris.model.Employee;
import com.hris.model.EmployeeJobHistory;
import com.hris.model.enums.ChangeType;
import com.hris.model.enums.EmployeeStatus;
import com.hris.model.enums.EmploymentStatus;
import com.hris.model.enums.SalaryChangeType;
import com.hris.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Employee Service
 * Handles business logic for employee management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeJobHistoryService jobHistoryService;
    private final SalaryHistoryService salaryHistoryService;
    private final ContractHistoryService contractHistoryService;

    private static final String UPLOAD_DIR = "uploads/photos/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all active employees
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAllActive();
    }

    /**
     * Search employees with filters and pagination
     */
    public Page<Employee> searchEmployees(String search, EmployeeStatus status, Long departmentId, EmploymentStatus employmentStatus, Pageable pageable) {
        log.info("Searching employees - search: {}, status: {}, departmentId: {}, employmentStatus: {}",
            search, status, departmentId, employmentStatus);
        return employeeRepository.searchEmployees(search, status, departmentId, employmentStatus, pageable);
    }

    /**
     * Get employee by ID
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findEmployeeByIdWithRelationships(id).orElse(null);
    }

    /**
     * Get employee by NIK
     */
    public Employee getEmployeeByNik(String nik) {
        return employeeRepository.findByNikAndDeletedAtIsNull(nik).orElse(null);
    }

    /**
     * Create new employee
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        log.info("Creating new employee: {} ({})", employee.getFullName(), employee.getNik());

        // Validate: NIK must be unique
        if (employeeRepository.existsByNikAndDeletedAtIsNull(employee.getNik())) {
            throw new IllegalArgumentException("NIK sudah terdaftar: " + employee.getNik());
        }

        // Validate: Email must be unique
        if (employeeRepository.existsByEmailAndDeletedAtIsNull(employee.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar: " + employee.getEmail());
        }

        // Validate: BPJS Ketenagakerjaan uniqueness
        if (employee.getBpjsKetenagakerjaanNo() != null &&
            employeeRepository.existsByBpjsKetenagakerjaanNoAndDeletedAtIsNull(employee.getBpjsKetenagakerjaanNo())) {
            throw new IllegalArgumentException("No BPJS Ketenagakerjaan sudah terdaftar: " + employee.getBpjsKetenagakerjaanNo());
        }

        // Validate: BPJS Kesehatan uniqueness
        if (employee.getBpjsKesehatanNo() != null &&
            employeeRepository.existsByBpjsKesehatanNoAndDeletedAtIsNull(employee.getBpjsKesehatanNo())) {
            throw new IllegalArgumentException("No BPJS Kesehatan sudah terdaftar: " + employee.getBpjsKesehatanNo());
        }

        // Validate: NPWP uniqueness
        if (employee.getNpwp() != null &&
            employeeRepository.existsByNpwpAndDeletedAtIsNull(employee.getNpwp())) {
            throw new IllegalArgumentException("NPWP sudah terdaftar: " + employee.getNpwp());
        }

        // Validate: date of birth (must be at least 17 years old)
        if (employee.getDateOfBirth() != null) {
            LocalDate minBirthDate = LocalDate.now().minusYears(17);
            if (employee.getDateOfBirth().isAfter(minBirthDate)) {
                throw new IllegalArgumentException("Usia minimum adalah 17 tahun");
            }
        }

        // Validate: hire date (must be on or before today)
        if (employee.getHireDate() != null && employee.getHireDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal bergabung tidak boleh di masa depan");
        }

        // Encode password
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        } else {
            // Set default password: NIK + birth year (last 4 digits)
            String defaultPassword = employee.getNik() + employee.getDateOfBirth().toString().substring(0, 4);
            employee.setPassword(passwordEncoder.encode(defaultPassword));
        }

        // Set default status if not provided
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        // Set default employment status if not provided (default: PROBATION for new hires)
        if (employee.getEmploymentStatus() == null) {
            employee.setEmploymentStatus(com.hris.model.enums.EmploymentStatus.PROBATION);
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", saved.getId());

        // Record initial employment status in contract history
        LocalDate effectiveDate = saved.getHireDate() != null ? saved.getHireDate() : LocalDate.now();
        contractHistoryService.createInitialEmployment(
                saved.getId(),
                saved.getEmploymentStatus(),
                effectiveDate
        );
        log.info("Initial contract history recorded for employee ID: {} with status: {}",
                saved.getId(), saved.getEmploymentStatus());

        // Record initial job history
        jobHistoryService.recordNewHire(
                saved,
                saved.getDepartment(),
                saved.getPosition(),
                saved.getHireDate() != null ? saved.getHireDate() : LocalDate.now(),
                saved.getBasicSalary()
        );

        // Record initial salary history
        salaryHistoryService.recordInitialSalary(
                saved,
                saved.getBasicSalary(),
                saved.getHireDate() != null ? saved.getHireDate() : LocalDate.now(),
                saved.getId(), // created by the employee themselves initially
                jobHistoryService.getCurrentJob(saved.getId())
        );

        log.info("Initial job and salary history recorded for employee ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update existing employee
     */
    @Transactional
    public Employee updateEmployee(Long id, Employee employee) {
        log.info("Updating employee ID: {}", id);

        Employee existing = getEmployeeById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Employee tidak ditemukan dengan ID: " + id);
        }

        // Store old values for history tracking
        Long oldDepartmentId = existing.getDepartment() != null ? existing.getDepartment().getId() : null;
        Long oldPositionId = existing.getPosition() != null ? existing.getPosition().getId() : null;
        java.math.BigDecimal oldSalary = existing.getBasicSalary();
        EmployeeStatus oldStatus = existing.getStatus();

        // Validate: NIK uniqueness (exclude current employee)
        if (!existing.getNik().equals(employee.getNik()) &&
            employeeRepository.existsByNikAndDeletedAtIsNull(employee.getNik())) {
            throw new IllegalArgumentException("NIK sudah terdaftar: " + employee.getNik());
        }

        // Validate: Email uniqueness (exclude current employee)
        if (!existing.getEmail().equals(employee.getEmail()) &&
            employeeRepository.existsByEmailAndDeletedAtIsNull(employee.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar: " + employee.getEmail());
        }

        // Validate: BPJS uniqueness
        if (employee.getBpjsKetenagakerjaanNo() != null &&
            !employee.getBpjsKetenagakerjaanNo().equals(existing.getBpjsKetenagakerjaanNo()) &&
            employeeRepository.existsByBpjsKetenagakerjaanNoAndDeletedAtIsNull(employee.getBpjsKetenagakerjaanNo())) {
            throw new IllegalArgumentException("No BPJS Ketenagakerjaan sudah terdaftar: " + employee.getBpjsKetenagakerjaanNo());
        }

        if (employee.getBpjsKesehatanNo() != null &&
            !employee.getBpjsKesehatanNo().equals(existing.getBpjsKesehatanNo()) &&
            employeeRepository.existsByBpjsKesehatanNoAndDeletedAtIsNull(employee.getBpjsKesehatanNo())) {
            throw new IllegalArgumentException("No BPJS Kesehatan sudah terdaftar: " + employee.getBpjsKesehatanNo());
        }

        if (employee.getNpwp() != null &&
            !employee.getNpwp().equals(existing.getNpwp()) &&
            employeeRepository.existsByNpwpAndDeletedAtIsNull(employee.getNpwp())) {
            throw new IllegalArgumentException("NPWP sudah terdaftar: " + employee.getNpwp());
        }

        // Validate: For RESIGNED or FIRED status, resignation date and reason are required
        if ((employee.getStatus() == EmployeeStatus.RESIGNED || employee.getStatus() == EmployeeStatus.FIRED)) {
            if (employee.getResignationDate() == null) {
                throw new IllegalArgumentException("Tanggal keluar harus diisi untuk status " + employee.getStatus().getDisplayName());
            }
            if (employee.getResignationReason() == null || employee.getResignationReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Alasan keluar harus diisi untuk status " + employee.getStatus().getDisplayName());
            }
        }

        // Update fields
        existing.setNik(employee.getNik());
        existing.setFullName(employee.getFullName());
        existing.setPlaceOfBirth(employee.getPlaceOfBirth());
        existing.setDateOfBirth(employee.getDateOfBirth());
        existing.setGender(employee.getGender());
        existing.setMothersName(employee.getMothersName());
        existing.setAddress(employee.getAddress());
        existing.setPhone(employee.getPhone());
        existing.setEmail(employee.getEmail());
        existing.setEmploymentStatus(employee.getEmploymentStatus());
        existing.setHireDate(employee.getHireDate());
        existing.setWorkLocation(employee.getWorkLocation());
        existing.setBpjsKetenagakerjaanNo(employee.getBpjsKetenagakerjaanNo());
        existing.setBpjsKesehatanNo(employee.getBpjsKesehatanNo());
        existing.setNpwp(employee.getNpwp());
        existing.setBasicSalary(employee.getBasicSalary());
        existing.setDepartment(employee.getDepartment());
        existing.setPosition(employee.getPosition());
        existing.setApprover(employee.getApprover());
        existing.setKkNumber(employee.getKkNumber());
        existing.setMaritalStatus(employee.getMaritalStatus());
        existing.setSpouseName(employee.getSpouseName());
        existing.setNumberOfDependents(employee.getNumberOfDependents());
        existing.setStatus(employee.getStatus());
        existing.setResignationDate(employee.getResignationDate());
        existing.setResignationReason(employee.getResignationReason());

        // Update password only if provided
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(employee.getPassword()));
        }

        Employee saved = employeeRepository.save(existing);
        log.info("Employee updated successfully: {}", saved.getId());

        // Record history based on changes
        LocalDate effectiveDate = LocalDate.now();
        Long currentUserId = id; // In a real app, this would be the logged-in user ID

        // Check for department/position changes (job history)
        Long newDepartmentId = saved.getDepartment() != null ? saved.getDepartment().getId() : null;
        Long newPositionId = saved.getPosition() != null ? saved.getPosition().getId() : null;

        if (!java.util.Objects.equals(oldDepartmentId, newDepartmentId) ||
            !java.util.Objects.equals(oldPositionId, newPositionId)) {

            // Determine change type
            com.hris.model.enums.ChangeType changeType = com.hris.model.enums.ChangeType.STATUS_CHANGE;
            String reason = "Update job assignment";

            // Check if it's a promotion (position level increased)
            if (oldPositionId != null && newPositionId != null && !oldPositionId.equals(newPositionId)) {
                if (saved.getPosition().getLevel() > existing.getPosition().getLevel()) {
                    changeType = com.hris.model.enums.ChangeType.PROMOTION;
                    reason = "Promosi ke " + saved.getPosition().getName();
                } else if (saved.getPosition().getLevel() < existing.getPosition().getLevel()) {
                    changeType = com.hris.model.enums.ChangeType.DEMOTION;
                    reason = "Demosi ke " + saved.getPosition().getName();
                }
            }

            // Check if it's a transfer (department changed)
            if (!java.util.Objects.equals(oldDepartmentId, newDepartmentId) &&
                changeType == com.hris.model.enums.ChangeType.STATUS_CHANGE) {
                changeType = com.hris.model.enums.ChangeType.TRANSFER;
                reason = "Mutasi ke " + saved.getDepartment().getName();
            }

            // Record job history
            EmployeeJobHistory jobHistory = jobHistoryService.recordJobHistory(
                    saved,
                    saved.getDepartment(),
                    saved.getPosition(),
                    changeType,
                    effectiveDate,
                    reason,
                    saved.getBasicSalary()
            );

            log.info("Job history recorded for employee ID: {}, type: {}", saved.getId(), changeType);
        }

        // Check for salary changes
        if (!java.util.Objects.equals(oldSalary, saved.getBasicSalary()) &&
            saved.getBasicSalary() != null && saved.getBasicSalary().compareTo(java.math.BigDecimal.ZERO) > 0) {

            com.hris.model.enums.SalaryChangeType salaryChangeType;
            String reason;

            if (oldSalary == null || oldSalary.compareTo(java.math.BigDecimal.ZERO) == 0) {
                salaryChangeType = com.hris.model.enums.SalaryChangeType.INITIAL;
                reason = "Set gaji awal";
            } else if (saved.getBasicSalary().compareTo(oldSalary) > 0) {
                salaryChangeType = com.hris.model.enums.SalaryChangeType.INCREASE;
                reason = "Kenaikan gaji";
            } else {
                salaryChangeType = com.hris.model.enums.SalaryChangeType.DECREASE;
                reason = "Penurunan gaji";
            }

            salaryHistoryService.recordSalaryChange(
                    saved,
                    oldSalary,
                    saved.getBasicSalary(),
                    salaryChangeType,
                    effectiveDate,
                    reason,
                    currentUserId,
                    null
            );

            log.info("Salary history recorded for employee ID: {}, type: {}", saved.getId(), salaryChangeType);
        }

        // Check for resignation
        if (oldStatus == EmployeeStatus.ACTIVE && saved.getStatus() == EmployeeStatus.RESIGNED) {
            LocalDate resignationDate = saved.getResignationDate() != null ? saved.getResignationDate() : LocalDate.now();
            jobHistoryService.recordResignation(
                    saved,
                    resignationDate,
                    saved.getResignationReason() != null ? saved.getResignationReason() : "Resignasi"
            );
            log.info("Resignation recorded for employee ID: {}", saved.getId());
        }

        return saved;
    }

    /**
     * Soft delete employee
     */
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Deleting employee ID: {}", id);

        Employee employee = getEmployeeById(id);
        if (employee == null) {
            throw new IllegalArgumentException("Employee tidak ditemukan dengan ID: " + id);
        }

        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);

        log.info("Employee deleted successfully: {}", id);
    }

    // =====================================================
    // PHOTO UPLOAD
    // =====================================================

    /**
     * Upload employee photo
     */
    @Transactional
    public String uploadPhoto(Long employeeId, MultipartFile file) throws IOException {
        log.info("Uploading photo for employee ID: {}", employeeId);

        Employee employee = getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee tidak ditemukan dengan ID: " + employeeId);
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Ukuran file maksimal 2MB");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("File harus berupa gambar");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = "employee_" + employeeId + "_" + UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        // Delete old photo if exists
        if (employee.getPhotoPath() != null) {
            Path oldPhoto = Paths.get(employee.getPhotoPath());
            if (Files.exists(oldPhoto)) {
                Files.delete(oldPhoto);
            }
        }

        // Update employee
        employee.setPhotoPath(filePath.toString());
        employeeRepository.save(employee);

        log.info("Photo uploaded successfully: {}", filePath);
        return filePath.toString();
    }

    /**
     * Delete employee photo
     */
    @Transactional
    public void deletePhoto(Long employeeId) throws IOException {
        log.info("Deleting photo for employee ID: {}", employeeId);

        Employee employee = getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee tidak ditemukan dengan ID: " + employeeId);
        }

        if (employee.getPhotoPath() != null) {
            Path photoPath = Paths.get(employee.getPhotoPath());
            if (Files.exists(photoPath)) {
                Files.delete(photoPath);
            }
            employee.setPhotoPath(null);
            employeeRepository.save(employee);
        }

        log.info("Photo deleted successfully for employee ID: {}", employeeId);
    }

    // =====================================================
    // APPROVER MANAGEMENT
    // =====================================================

    /**
     * Set approver for employee
     */
    @Transactional
    public Employee setApprover(Long employeeId, Long approverId) {
        log.info("Setting approver for employee ID: {} -> approver ID: {}", employeeId, approverId);

        Employee employee = getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee tidak ditemukan dengan ID: " + employeeId);
        }

        if (approverId != null) {
            Employee approver = getEmployeeById(approverId);
            if (approver == null) {
                throw new IllegalArgumentException("Approver tidak ditemukan dengan ID: " + approverId);
            }

            // Prevent self-approval
            if (approverId.equals(employeeId)) {
                throw new IllegalArgumentException("Tidak dapat menyetujui diri sendiri");
            }

            employee.setApprover(approver);
        } else {
            employee.setApprover(null);
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Approver set successfully for employee ID: {}", employeeId);
        return saved;
    }

    // =====================================================
    // QUERY METHODS
    // =====================================================

    /**
     * Get employees by department
     */
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId);
    }

    /**
     * Get employees by status
     */
    public List<Employee> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatusAndDeletedAtIsNull(status);
    }

    /**
     * Count employees by department
     */
    public long countByDepartment(Long departmentId) {
        return employeeRepository.countByDepartmentIdAndDeletedAtIsNull(departmentId);
    }

    /**
     * Count employees by status
     */
    public long countByStatus(EmployeeStatus status) {
        return employeeRepository.countByStatusAndDeletedAtIsNull(status);
    }

    /**
     * Count total active employees
     */
    public long countActiveEmployees() {
        return employeeRepository.countByDeletedAtIsNull();
    }
}
