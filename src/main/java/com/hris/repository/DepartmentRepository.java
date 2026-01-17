package com.hris.repository;

import com.hris.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Department entity
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Find all root departments (departments without parent)
     */
    @Query("SELECT d FROM Department d WHERE d.parent IS NULL AND d.deletedAt IS NULL ORDER BY d.name")
    List<Department> findRootDepartments();

    /**
     * Find child departments by parent ID
     */
    @Query("SELECT d FROM Department d WHERE d.parent.id = :parentId AND d.deletedAt IS NULL ORDER BY d.name")
    List<Department> findByParentIdAndDeletedAtIsNull(@Param("parentId") Long parentId);

    /**
     * Find department by head ID
     */
    @Query("SELECT d FROM Department d WHERE d.head.id = :headId AND d.deletedAt IS NULL")
    Optional<Department> findByHeadIdAndDeletedAtIsNull(@Param("headId") Long headId);

    /**
     * Find all active departments
     */
    @Query("SELECT d FROM Department d WHERE d.deletedAt IS NULL ORDER BY d.name")
    List<Department> findAllActive();

    /**
     * Find department by ID with head eagerly fetched
     * This allows accessing the head relationship outside of a transaction
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.head LEFT JOIN FETCH d.parent WHERE d.id = :id AND d.deletedAt IS NULL")
    Optional<Department> findByIdWithHeadAndParent(@Param("id") Long id);

    /**
     * Find department by name (case-insensitive)
     */
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) = LOWER(:name) AND d.deletedAt IS NULL")
    Optional<Department> findByNameIgnoreCaseAndDeletedAtIsNull(@Param("name") String name);

    /**
     * Check if department name exists
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Department d WHERE LOWER(d.name) = LOWER(:name) AND d.deletedAt IS NULL")
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(@Param("name") String name);

    /**
     * Find all prodis (departments where is_prodi = true)
     */
    @Query("SELECT d FROM Department d WHERE d.isProdi = true AND d.deletedAt IS NULL ORDER BY d.name")
    List<Department> findByIsProdiTrueAndDeletedAtIsNull();

    /**
     * Find all departments in a parent chain (for hierarchy validation)
     */
    @Query("SELECT d FROM Department d WHERE d.id IN :departmentIds AND d.deletedAt IS NULL")
    List<Department> findByIdsAndDeletedAtIsNull(@Param("departmentIds") List<Long> departmentIds);

    /**
     * Search departments with filters and pagination
     * Uses EntityGraph to eagerly load parent and head relationships
     * This prevents LazyInitializationException when accessing these properties in templates
     */
    @EntityGraph(attributePaths = {"parent", "head"})
    @Query("SELECT d FROM Department d WHERE " +
           "(:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:parentId IS NULL OR d.parent.id = :parentId) AND " +
           "(:isProdi IS NULL OR d.isProdi = :isProdi) AND " +
           "d.deletedAt IS NULL")
    Page<Department> searchDepartments(
            @Param("search") String search,
            @Param("parentId") Long parentId,
            @Param("isProdi") Boolean isProdi,
            Pageable pageable
    );
}
