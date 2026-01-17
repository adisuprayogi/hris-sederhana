package com.hris.repository;

import com.hris.model.Employee;
import com.hris.model.enums.EmployeeStatus;
import com.hris.model.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
     * Find all active employees
     */
    @Query("SELECT e FROM Employee e WHERE e.deletedAt IS NULL ORDER BY e.fullName")
    List<Employee> findAllActive();

    /**
     * Check if email exists (for unique validation)
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Check if NIK exists (for unique validation)
     */
    boolean existsByNikAndDeletedAtIsNull(String nik);

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
