package com.hris.service;

import com.hris.model.ContractHistory;
import com.hris.model.Employee;
import com.hris.model.enums.EmploymentStatus;
import com.hris.model.enums.EmploymentStatusChange;
import com.hris.repository.ContractHistoryRepository;
import com.hris.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service untuk ContractHistory
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractHistoryService {

    private final ContractHistoryRepository contractHistoryRepository;
    private final EmployeeRepository employeeRepository;

    private static final String UPLOAD_DIR = "uploads/contract-docs/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Get all contract history for an employee
     */
    public List<ContractHistory> getContractHistoryByEmployeeId(Long employeeId) {
        return contractHistoryRepository.findByEmployeeIdAndDeletedAtIsNullOrderByStartDateDesc(employeeId);
    }

    /**
     * Get contract history by ID
     */
    public Optional<ContractHistory> getContractHistoryById(Long id) {
        return contractHistoryRepository.findById(id);
    }

    /**
     * Get current employment status for an employee
     * Returns the latest record (either with no end date or the most recent one)
     */
    public Optional<ContractHistory> getCurrentEmploymentStatus(Long employeeId) {
        // First try to find a record with no end date (active status)
        Optional<ContractHistory> activeStatus =
            contractHistoryRepository.findByEmployeeIdAndEndDateIsNullAndDeletedAtIsNullOrderByStartDateDesc(employeeId);

        if (activeStatus.isPresent()) {
            return activeStatus;
        }

        // If no active status found, get the latest record regardless of end date
        List<ContractHistory> allHistory = contractHistoryRepository.findByEmployeeIdAndDeletedAtIsNullOrderByStartDateDesc(employeeId);
        return allHistory.isEmpty() ? Optional.empty() : Optional.of(allHistory.get(0));
    }

    /**
     * Get all contract periods for an employee
     */
    public List<ContractHistory> getContractPeriods(Long employeeId) {
        return contractHistoryRepository.findByEmployeeIdAndNewStatusAndDeletedAtIsNullOrderByStartDateDesc(
            employeeId, EmploymentStatus.CONTRACT
        );
    }

    /**
     * Get permanent appointment history for an employee
     */
    public List<ContractHistory> getPermanentAppointmentHistory(Long employeeId) {
        return contractHistoryRepository.findPermanentAppointmentHistory(employeeId);
    }

    /**
     * Create initial employment record (when hiring new employee)
     */
    @Transactional
    public ContractHistory createInitialEmployment(Long employeeId, EmploymentStatus initialStatus, LocalDate startDate) {
        Employee employee = employeeRepository.findEmployeeByIdWithRelationships(employeeId).orElse(null);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        // Check if employee already has initial employment record
        Optional<ContractHistory> existing = getCurrentEmploymentStatus(employeeId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Employee already has employment status record");
        }

        ContractHistory history = new ContractHistory();
        history.setEmployee(employee);
        history.setChangeType(EmploymentStatusChange.INITIAL_HIRING);
        history.setOldStatus(null);
        history.setNewStatus(initialStatus);
        history.setStartDate(startDate);
        history.setEndDate(null);
        history.setPermanentAppointmentDate(null);

        if (initialStatus == EmploymentStatus.PERMANENT) {
            history.setPermanentAppointmentDate(startDate);
        }

        return contractHistoryRepository.save(history);
    }

    /**
     * Record employment status change
     */
    @Transactional
    public ContractHistory recordStatusChange(Long employeeId,
                                              EmploymentStatusChange changeType,
                                              EmploymentStatus newStatus,
                                              LocalDate effectiveDate,
                                              String reason,
                                              String notes) {
        Employee employee = employeeRepository.findEmployeeByIdWithRelationships(employeeId).orElse(null);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        // Get current employment status
        Optional<ContractHistory> currentStatus = getCurrentEmploymentStatus(employeeId);

        // End current status if exists
        if (currentStatus.isPresent()) {
            ContractHistory current = currentStatus.get();
            // End the day before new status starts
            current.setEndDate(effectiveDate.minusDays(1));
            contractHistoryRepository.save(current);
        }

        // Create new status record
        ContractHistory newHistory = new ContractHistory();
        newHistory.setEmployee(employee);
        newHistory.setChangeType(changeType);
        newHistory.setOldStatus(currentStatus.map(ContractHistory::getNewStatus).orElse(null));
        newHistory.setNewStatus(newStatus);
        newHistory.setStartDate(effectiveDate);
        newHistory.setReason(reason);
        newHistory.setNotes(notes);

        // Set permanent appointment date if applicable
        if (newStatus == EmploymentStatus.PERMANENT) {
            newHistory.setPermanentAppointmentDate(effectiveDate);
        }

        // Update employee's current employment status
        employee.setEmploymentStatus(newStatus);
        employeeRepository.save(employee);

        return contractHistoryRepository.save(newHistory);
    }

    /**
     * Record contract renewal
     */
    @Transactional
    public ContractHistory recordContractRenewal(Long employeeId,
                                                  LocalDate newStartDate,
                                                  LocalDate newEndDate,
                                                  String newContractNumber,
                                                  String reason) {
        Employee employee = employeeRepository.findEmployeeByIdWithRelationships(employeeId).orElse(null);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        // End current contract
        Optional<ContractHistory> currentContract = getCurrentEmploymentStatus(employeeId);
        if (currentContract.isPresent()) {
            ContractHistory current = currentContract.get();
            current.setEndDate(newStartDate.minusDays(1));
            contractHistoryRepository.save(current);
        }

        // Create new contract record
        ContractHistory newContract = new ContractHistory();
        newContract.setEmployee(employee);
        newContract.setChangeType(EmploymentStatusChange.CONTRACT_RENEWAL);
        newContract.setOldStatus(currentContract.map(ContractHistory::getNewStatus).orElse(null));
        newContract.setNewStatus(EmploymentStatus.CONTRACT);
        newContract.setStartDate(newStartDate);
        newContract.setEndDate(newEndDate);
        newContract.setContractNumber(newContractNumber);
        newContract.setReason(reason);

        return contractHistoryRepository.save(newContract);
    }

    /**
     * Record permanent appointment (from contract or probation)
     */
    @Transactional
    public ContractHistory recordPermanentAppointment(Long employeeId,
                                                       LocalDate appointmentDate,
                                                       String reason) {
        Employee employee = employeeRepository.findEmployeeByIdWithRelationships(employeeId).orElse(null);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found");
        }

        Optional<ContractHistory> currentStatus = getCurrentEmploymentStatus(employeeId);

        EmploymentStatusChange changeType;
        if (currentStatus.isPresent()) {
            EmploymentStatus currentStatusEnum = currentStatus.get().getNewStatus();
            if (currentStatusEnum == EmploymentStatus.PROBATION) {
                changeType = EmploymentStatusChange.PROBATION_TO_PERMANENT;
            } else if (currentStatusEnum == EmploymentStatus.CONTRACT) {
                changeType = EmploymentStatusChange.CONTRACT_TO_PERMANENT;
            } else {
                throw new IllegalStateException("Cannot convert current status to permanent");
            }
        } else {
            throw new IllegalStateException("No current employment status found");
        }

        return recordStatusChange(employeeId, changeType, EmploymentStatus.PERMANENT,
            appointmentDate, reason, null);
    }

    /**
     * Get contracts expiring soon (within next 30 days)
     */
    public List<ContractHistory> getContractsExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(30);
        return contractHistoryRepository.findContractsExpiringSoon(today, expiryDate);
    }

    /**
     * Get all active contracts
     */
    public List<ContractHistory> getAllActiveContracts() {
        return contractHistoryRepository.findAllActiveContracts(LocalDate.now());
    }

    /**
     * Check if employee has active contract
     */
    public boolean hasActiveContract(Long employeeId) {
        return contractHistoryRepository.hasActiveContract(employeeId);
    }

    /**
     * Get employment history count
     */
    public long getEmploymentHistoryCount(Long employeeId) {
        return contractHistoryRepository.countByEmployeeId(employeeId);
    }

    /**
     * Delete contract history (soft delete)
     */
    @Transactional
    public void deleteContractHistory(Long id) {
        ContractHistory history = contractHistoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contract history not found"));

        history.softDelete(null);
        contractHistoryRepository.save(history);
    }

    /**
     * Update contract history
     */
    @Transactional
    public ContractHistory updateContractHistory(Long id, ContractHistory updatedHistory) {
        ContractHistory existing = contractHistoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contract history not found"));

        existing.setChangeType(updatedHistory.getChangeType());
        existing.setNewStatus(updatedHistory.getNewStatus());
        existing.setStartDate(updatedHistory.getStartDate());
        existing.setEndDate(updatedHistory.getEndDate());
        existing.setContractNumber(updatedHistory.getContractNumber());
        existing.setReason(updatedHistory.getReason());
        existing.setNotes(updatedHistory.getNotes());
        existing.setDocumentPath(updatedHistory.getDocumentPath());

        return contractHistoryRepository.save(existing);
    }

    /**
     * Upload document for contract history
     */
    @Transactional
    public String uploadDocument(Long contractHistoryId, MultipartFile file) throws IOException {
        log.info("Uploading document for contract history ID: {}", contractHistoryId);

        ContractHistory history = contractHistoryRepository.findById(contractHistoryId)
            .orElseThrow(() -> new IllegalArgumentException("Contract history not found"));

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Ukuran file maksimal 5MB");
        }

        // Validate file type (PDF only)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("File harus berupa PDF");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".pdf";
        String filename = "contract_" + contractHistoryId + "_" + UUID.randomUUID() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        // Delete old document if exists
        if (history.getDocumentPath() != null) {
            Path oldDoc = Paths.get(history.getDocumentPath());
            if (Files.exists(oldDoc)) {
                Files.delete(oldDoc);
            }
        }

        // Update contract history
        history.setDocumentPath(filePath.toString());
        contractHistoryRepository.save(history);

        log.info("Document uploaded successfully: {}", filePath);
        return filePath.toString();
    }

    /**
     * Get contract history statistics
     */
    public List<ContractHistory> getAllHistory() {
        return contractHistoryRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate"));
    }
}
