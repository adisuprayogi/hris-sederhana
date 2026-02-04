package com.hris.repository;

import com.hris.model.EmployeeRole;
import com.hris.model.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for EmployeeRole entity
 */
@Repository
public interface EmployeeRoleRepository extends JpaRepository<EmployeeRole, Long> {

    /**
     * Find all active roles for an employee
     */
    @Query("SELECT er.role FROM EmployeeRole er " +
           "WHERE er.employeeId = :employeeId AND er.deletedAt IS NULL")
    Set<RoleType> findRolesByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Check if employee has a specific role
     */
    @Query("SELECT COUNT(er) > 0 FROM EmployeeRole er " +
           "WHERE er.employeeId = :employeeId AND er.role = :role AND er.deletedAt IS NULL")
    boolean existsByEmployeeIdAndRole(@Param("employeeId") Long employeeId, @Param("role") RoleType role);

    /**
     * Find all employees with multiple roles
     */
    @Query("SELECT er.employeeId FROM EmployeeRole er " +
           "WHERE er.deletedAt IS NULL " +
           "GROUP BY er.employeeId " +
           "HAVING COUNT(er.id) > 1")
    List<Long> findEmployeeIdsWithMultipleRoles();

    /**
     * Delete all roles for an employee (soft delete)
     */
    @Query("UPDATE EmployeeRole er SET er.deletedAt = CURRENT_TIMESTAMP, er.deletedBy = :deletedBy " +
           "WHERE er.employeeId = :employeeId AND er.deletedAt IS NULL")
    void softDeleteByEmployeeId(@Param("employeeId") Long employeeId, @Param("deletedBy") Long deletedBy);

    /**
     * Find employee role by employee ID and role type (active only)
     */
    Optional<EmployeeRole> findByEmployeeIdAndRoleAndDeletedAtIsNull(Long employeeId, RoleType role);
}
