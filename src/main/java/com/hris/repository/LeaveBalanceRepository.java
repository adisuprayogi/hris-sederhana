package com.hris.repository;

import com.hris.model.Employee;
import com.hris.model.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository untuk LeaveBalance Entity
 */
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    /**
     * Find leave balance by employee and year
     */
    Optional<LeaveBalance> findByEmployeeIdAndYearAndDeletedAtIsNull(Long employeeId, Integer year);

    /**
     * Find all leave balances by employee
     */
    List<LeaveBalance> findByEmployeeIdAndDeletedAtIsNullOrderByYearDesc(Long employeeId);

    /**
     * Find leave balances by year
     */
    List<LeaveBalance> findByYearAndDeletedAtIsNull(Integer year);

    /**
     * Find leave balances by year ordered by employee name
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.year = :year AND lb.deletedAt IS NULL ORDER BY lb.employee.fullName ASC")
    List<LeaveBalance> findByYearAndDeletedAtIsNullOrderByEmployeeFullNameAsc(@Param("year") Integer year);

    /**
     * Search leave balances by employee name and year
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.fullName LIKE %:name% AND lb.year = :year AND lb.deletedAt IS NULL ORDER BY lb.employee.fullName ASC")
    List<LeaveBalance> searchByEmployeeNameAndYear(@Param("name") String name, @Param("year") Integer year);

    /**
     * Find leave balances by department and year
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.department.id = :departmentId AND lb.year = :year AND lb.deletedAt IS NULL ORDER BY lb.employee.fullName ASC")
    List<LeaveBalance> findByDepartmentAndYear(@Param("departmentId") Long departmentId, @Param("year") Integer year);

    /**
     * Find current year leave balance for employee
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee.id = :employeeId AND lb.year = :year AND lb.deletedAt IS NULL")
    Optional<LeaveBalance> findCurrentBalance(@Param("employeeId") Long employeeId, @Param("year") Integer year);

    /**
     * Check if leave balance exists for employee and year
     */
    boolean existsByEmployeeIdAndYearAndDeletedAtIsNull(Long employeeId, Integer year);

    /**
     * Get all employees with expiring carried forward leave
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.carriedForwardExpiryDate IS NOT NULL " +
            "AND lb.carriedForward > 0 " +
            "AND lb.deletedAt IS NULL")
    List<LeaveBalance> findExpiringCarriedForwardBalances();

    /**
     * Get leave balances with low remaining (less than threshold)
     */
    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employee = :employee " +
            "AND (lb.balance - lb.used) < :threshold " +
            "AND lb.deletedAt IS NULL")
    List<LeaveBalance> findLowBalanceByEmployee(@Param("employee") Employee employee, @Param("threshold") double threshold);
}
