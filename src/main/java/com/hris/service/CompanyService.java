package com.hris.service;

import com.hris.model.Company;
import com.hris.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Company Service
 * Handles business logic for company management
 * Note: Company is a singleton - only one active record should exist
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    private static final String LOGO_UPLOAD_DIR = "uploads/company/logo/";
    private static final String STAMP_UPLOAD_DIR = "uploads/company/stamp/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get the active company (singleton)
     */
    @Transactional(readOnly = true)
    public Company getCompany() {
        return companyRepository.findFirstByDeletedAtIsNullOrderByCreatedAtDesc().orElse(null);
    }

    /**
     * Get company by ID
     */
    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    /**
     * Create or update company
     * For singleton pattern, this updates the existing company if one exists
     */
    @Transactional
    public Company saveCompany(Company company) {
        // Check if there's an existing active company
        Company existing = getCompany();

        if (existing != null && !existing.getId().equals(company.getId())) {
            throw new IllegalArgumentException(
                "Hanya boleh ada satu data perusahaan. Silakan edit data yang sudah ada."
            );
        }

        // Validate unique code
        if (company.getCode() != null) {
            Company existingByCode = companyRepository.findByCodeAndDeletedAtIsNull(company.getCode()).orElse(null);
            if (existingByCode != null && !existingByCode.getId().equals(company.getId())) {
                throw new IllegalArgumentException("Kode perusahaan sudah digunakan: " + company.getCode());
            }
        }

        // Validate unique NPWP
        if (company.getNpwpCompany() != null) {
            Company existingByNpwp = companyRepository.findById(company.getId()).orElse(null);
            // Note: NPWP has unique constraint in database, let database handle it
        }

        Company saved = companyRepository.save(company);
        log.info("Company saved successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Update existing company
     */
    @Transactional
    public Company updateCompany(Long id, Company company) {
        Company existing = getCompanyById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Data perusahaan tidak ditemukan dengan ID: " + id);
        }

        // Validate unique code (exclude current)
        if (company.getCode() != null && !company.getCode().equals(existing.getCode())) {
            if (companyRepository.existsByCodeAndDeletedAtIsNullAndIdNot(company.getCode(), id)) {
                throw new IllegalArgumentException("Kode perusahaan sudah digunakan: " + company.getCode());
            }
        }

        // Update fields
        existing.setName(company.getName());
        existing.setCode(company.getCode());
        existing.setType(company.getType());
        existing.setAddress(company.getAddress());
        existing.setCity(company.getCity());
        existing.setProvince(company.getProvince());
        existing.setPostalCode(company.getPostalCode());
        existing.setPhone(company.getPhone());
        existing.setEmail(company.getEmail());
        existing.setWebsite(company.getWebsite());
        existing.setNpwpCompany(company.getNpwpCompany());
        existing.setSiupNumber(company.getSiupNumber());
        existing.setSiupExpiredDate(company.getSiupExpiredDate());
        existing.setEstablishmentDate(company.getEstablishmentDate());
        existing.setBpjsKetenagakerjaanNo(company.getBpjsKetenagakerjaanNo());
        existing.setBpjsKesehatanNo(company.getBpjsKesehatanNo());
        existing.setTaxAddress(company.getTaxAddress());
        existing.setBankName(company.getBankName());
        existing.setBankAccountNumber(company.getBankAccountNumber());
        existing.setBankAccountName(company.getBankAccountName());
        existing.setWorkingDays(company.getWorkingDays());
        existing.setClockInStart(company.getClockInStart());
        existing.setClockInEnd(company.getClockInEnd());
        existing.setClockOutStart(company.getClockOutStart());
        existing.setClockOutEnd(company.getClockOutEnd());

        // Note: logo_path and stamp_path are updated separately via upload methods

        Company saved = companyRepository.save(existing);
        log.info("Company updated successfully: {}", saved.getId());
        return saved;
    }

    // =====================================================
    // FILE UPLOAD
    // =====================================================

    /**
     * Upload company logo
     */
    @Transactional
    public String uploadLogo(Long companyId, MultipartFile file) throws IOException {
        log.info("Uploading logo for company ID: {}", companyId);

        Company company = getCompanyById(companyId);
        if (company == null) {
            throw new IllegalArgumentException("Data perusahaan tidak ditemukan dengan ID: " + companyId);
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
        Path uploadPath = Paths.get(LOGO_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Delete old logo if exists
        if (company.getLogoPath() != null) {
            Path oldLogo = Paths.get(company.getLogoPath());
            if (Files.exists(oldLogo)) {
                Files.delete(oldLogo);
            }
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".png";
        String filename = "company_logo_" + UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        // Update company
        company.setLogoPath(filePath.toString());
        companyRepository.save(company);

        log.info("Logo uploaded successfully: {}", filePath);
        return filePath.toString();
    }

    /**
     * Upload company stamp
     */
    @Transactional
    public String uploadStamp(Long companyId, MultipartFile file) throws IOException {
        log.info("Uploading stamp for company ID: {}", companyId);

        Company company = getCompanyById(companyId);
        if (company == null) {
            throw new IllegalArgumentException("Data perusahaan tidak ditemukan dengan ID: " + companyId);
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
        Path uploadPath = Paths.get(STAMP_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Delete old stamp if exists
        if (company.getStampPath() != null) {
            Path oldStamp = Paths.get(company.getStampPath());
            if (Files.exists(oldStamp)) {
                Files.delete(oldStamp);
            }
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".png";
        String filename = "company_stamp_" + UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        // Update company
        company.setStampPath(filePath.toString());
        companyRepository.save(company);

        log.info("Stamp uploaded successfully: {}", filePath);
        return filePath.toString();
    }

    /**
     * Delete company logo
     */
    @Transactional
    public void deleteLogo(Long companyId) throws IOException {
        log.info("Deleting logo for company ID: {}", companyId);

        Company company = getCompanyById(companyId);
        if (company == null) {
            throw new IllegalArgumentException("Data perusahaan tidak ditemukan dengan ID: " + companyId);
        }

        if (company.getLogoPath() != null) {
            Path logoPath = Paths.get(company.getLogoPath());
            if (Files.exists(logoPath)) {
                Files.delete(logoPath);
            }
            company.setLogoPath(null);
            companyRepository.save(company);
        }

        log.info("Logo deleted successfully for company ID: {}", companyId);
    }

    /**
     * Delete company stamp
     */
    @Transactional
    public void deleteStamp(Long companyId) throws IOException {
        log.info("Deleting stamp for company ID: {}", companyId);

        Company company = getCompanyById(companyId);
        if (company == null) {
            throw new IllegalArgumentException("Data perusahaan tidak ditemukan dengan ID: " + companyId);
        }

        if (company.getStampPath() != null) {
            Path stampPath = Paths.get(company.getStampPath());
            if (Files.exists(stampPath)) {
                Files.delete(stampPath);
            }
            company.setStampPath(null);
            companyRepository.save(company);
        }

        log.info("Stamp deleted successfully for company ID: {}", companyId);
    }

    // =====================================================
    // QUERY METHODS
    // =====================================================

    /**
     * Check if company data exists
     */
    public boolean companyExists() {
        return getCompany() != null;
    }

    /**
     * Count total active companies
     */
    public long countCompanies() {
        return companyRepository.countByDeletedAtIsNull();
    }
}
