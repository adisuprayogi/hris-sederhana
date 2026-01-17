package com.hris.repository;

import com.hris.model.Employee;
import com.hris.model.enums.EmployeeStatus;
import com.hris.model.enums.EmploymentStatus;
import com.hris.model.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Employee entity
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find active employee by email
     */
    Optional<Employee> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Find active employee by NIK
     */
    Optional<Employee> findByNikAndDeletedAtIsNull(String nik);

    /**
     * Find all active employees by status
     */
    List<Employee> findByStatusAndDeletedAtIsNull(EmployeeStatus status);

    /**
     * Find all active employees by department
     */
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.deletedAt IS NULL")
    List<Employee> findByDepartmentIdAndDeletedAtIsNull(@Param("departmentId") Long departmentId);

    /**
     * Find all active employees (with JOIN FETCH for lazy relationships)
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "WHERE e.deletedAt IS NULL ORDER BY e.fullName")
    List<Employee> findAllActive();

    /**
     * Find employee by ID with relationships fetched (for detail page)
     */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "LEFT JOIN FETCH e.approver " +
           "WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Employee> findEmployeeByIdWithRelationships(@Param("id") Long id);

    /**
     * Search employees with filters and pagination (with JOIN FETCH for lazy relationships)
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.position " +
           "WHERE (:search IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.nik) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:employmentStatus IS NULL OR e.employmentStatus = :employmentStatus) AND " +
           "e.deletedAt IS NULL")
    Page<Employee> searchEmployees(
            @Param("search") String search,
            @Param("status") EmployeeStatus status,
            @Param("departmentId") Long departmentId,
            @Param("employmentStatus") EmploymentStatus employmentStatus,
            Pageable pageable
    );

    // =====================================================
    // EXISTENCE CHECKS (for validation)
    // =====================================================

    /**
     * Check if email exists (for unique validation)
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Check if NIK exists (for unique validation)
     */
    boolean existsByNikAndDeletedAtIsNull(String nik);

    /**
     * Check if BPJS Ketenagakerjaan number exists
     */
    boolean existsByBpjsKetenagakerjaanNoAndDeletedAtIsNull(String bpjsKetenagakerjaanNo);

    /**
     * Check if BPJS Kesehatan number exists
     */
    boolean existsByBpjsKesehatanNoAndDeletedAtIsNull(String bpjsKesehatanNo);

    /**
     * Check if NPWP exists
     */
    boolean existsByNpwpAndDeletedAtIsNull(String npwp);

    // =====================================================
    // COUNT METHODS
    // =====================================================

    /**
     * Count employees by department
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deletedAt IS NULL")
    long countByDepartmentIdAndDeletedAtIsNull(@Param("departmentId") Long departmentId);

    /**
     * Count employees by status
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status AND e.deletedAt IS NULL")
    long countByStatusAndDeletedAtIsNull(@Param("status") EmployeeStatus status);

    /**
     * Count all active employees
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.deletedAt IS NULL")
    long countByDeletedAtIsNull();

    // =====================================================
    // ROLE-BASED QUERIES
    // =====================================================

    /**
     * Find employees with specific role
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "JOIN EmployeeRole er ON e.id = er.employeeId " +
           "WHERE er.role = :role AND er.deletedAt IS NULL AND e.deletedAt IS NULL")
    List<Employee> findByRole(@Param("role") RoleType role);

    /**
     * Find employees with specific role by string
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "JOIN EmployeeRole er ON e.id = er.employeeId " +
           "WHERE er.role = :role AND er.deletedAt IS NULL AND e.deletedAt IS NULL")
    List<Employee> findByRole(@Param("role") String role);

    /**
     * Check if an employee has a specific role
     */
    @Query("SELECT CASE WHEN COUNT(er) > 0 THEN true ELSE false END " +
           "FROM EmployeeRole er " +
           "WHERE er.employeeId = :employeeId AND er.role = :role AND er.deletedAt IS NULL")
    boolean hasRole(@Param("employeeId") Long employeeId, @Param("role") String role);
}
