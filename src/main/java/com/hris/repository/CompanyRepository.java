package com.hris.repository;

import com.hris.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Company entity
 * Note: Company is a singleton - only one active record should exist
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Find active company (deleted_at IS NULL)
     * For singleton pattern, this should return at most 1 record
     */
    Optional<Company> findFirstByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * Find company by code
     */
    Optional<Company> findByCodeAndDeletedAtIsNull(String code);

    /**
     * Check if company exists by code (excluding current)
     */
    boolean existsByCodeAndDeletedAtIsNullAndIdNot(String code, Long id);

    /**
     * Check if company exists by NPWP (excluding current)
     */
    boolean existsByNpwpCompanyAndDeletedAtIsNullAndIdNot(String npwp, Long id);

    /**
     * Count total active companies
     * Should be 1 for singleton pattern
     */
    long countByDeletedAtIsNull();
}
