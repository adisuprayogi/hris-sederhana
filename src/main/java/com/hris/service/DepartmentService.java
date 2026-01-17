package com.hris.service;

import com.hris.model.Department;
import com.hris.model.Employee;
import com.hris.repository.DepartmentRepository;
import com.hris.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Department Service
 * Handles business logic for department management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all active departments
     */
    public List<Department> getAllDepartments() {
        return departmentRepository.findAllActive();
    }

    /**
     * Get all prodis (departments where is_prodi = true)
     */
    public List<Department> getAllProdis() {
        return departmentRepository.findByIsProdiTrueAndDeletedAtIsNull();
    }

    /**
     * Search departments with filters and pagination
     */
    public Page<Department> searchDepartments(String search, Long parentId, Boolean isProdi, Pageable pageable) {
        return departmentRepository.searchDepartments(search, parentId, isProdi, pageable);
    }

    /**
     * Get department by ID
     * @Transactional ensures lazy relationships can be accessed within the same session
     */
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .filter(dept -> dept.getDeletedAt() == null)
                .orElse(null);
    }

    /**
     * Get department by ID with head and parent eagerly loaded
     * Use this when you need to access head/parent outside of the transaction
     */
    @Transactional(readOnly = true)
    public Department getDepartmentWithHeadAndParent(Long id) {
        return departmentRepository.findByIdWithHeadAndParent(id).orElse(null);
    }

    /**
     * Create new department
     */
    @Transactional
    public Department createDepartment(Department department) {
        log.info("Creating new department: {}", department.getName());

        // Validate: name must be unique
        if (departmentRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(department.getName())) {
            throw new IllegalArgumentException("Nama department sudah ada: " + department.getName());
        }

        // Validate: parent department must exist and not create circular reference
        if (department.getParent() != null && department.getParent().getId() != null) {
            Department parent = getDepartmentById(department.getParent().getId());
            if (parent == null) {
                throw new IllegalArgumentException("Parent department tidak ditemukan");
            }
            department.setParent(parent);

            // Check for circular reference
            if (wouldCreateCircularReference(parent, department)) {
                throw new IllegalArgumentException("Tidak dapat membuat circular reference dalam hierarchy department");
            }
        }

        // Validate: head of department must exist
        if (department.getHead() != null && department.getHead().getId() != null) {
            Employee head = employeeRepository.findById(department.getHead().getId())
                    .filter(emp -> emp.getDeletedAt() == null)
                    .orElseThrow(() -> new IllegalArgumentException("Head of department tidak ditemukan"));
            department.setHead(head);
        }

        Department saved = departmentRepository.save(department);
        log.info("Department created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update existing department
     */
    @Transactional
    public Department updateDepartment(Long id, Department department) {
        log.info("Updating department ID: {}", id);

        Department existing = getDepartmentById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Department tidak ditemukan dengan ID: " + id);
        }

        // Validate: name uniqueness (exclude current department)
        if (!existing.getName().equalsIgnoreCase(department.getName()) &&
            departmentRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(department.getName())) {
            throw new IllegalArgumentException("Nama department sudah ada: " + department.getName());
        }

        // Update basic fields
        existing.setName(department.getName());
        existing.setDescription(department.getDescription());
        existing.setIsProdi(department.getIsProdi());
        existing.setKodeProdi(department.getKodeProdi());

        // Update parent
        if (department.getParent() != null && department.getParent().getId() != null) {
            Department parent = getDepartmentById(department.getParent().getId());
            if (parent == null) {
                throw new IllegalArgumentException("Parent department tidak ditemukan");
            }

            // Check for circular reference
            if (wouldCreateCircularReference(parent, existing)) {
                throw new IllegalArgumentException("Tidak dapat membuat circular reference dalam hierarchy department");
            }

            existing.setParent(parent);
        } else {
            existing.setParent(null);
        }

        // Update head
        if (department.getHead() != null && department.getHead().getId() != null) {
            Employee head = employeeRepository.findById(department.getHead().getId())
                    .filter(emp -> emp.getDeletedAt() == null)
                    .orElseThrow(() -> new IllegalArgumentException("Head of department tidak ditemukan"));
            existing.setHead(head);
        } else {
            existing.setHead(null);
        }

        Department saved = departmentRepository.save(existing);
        log.info("Department updated successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Soft delete department
     */
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department ID: {}", id);

        Department department = getDepartmentById(id);
        if (department == null) {
            throw new IllegalArgumentException("Department tidak ditemukan dengan ID: " + id);
        }

        // Validate: cannot delete if has children
        if (hasChildren(id)) {
            throw new IllegalArgumentException("Tidak dapat menghapus department yang memiliki sub-department. " +
                    "Silakan reassign atau hapus sub-department terlebih dahulu.");
        }

        // Validate: cannot delete if has employees
        if (hasEmployees(id)) {
            throw new IllegalArgumentException("Tidak dapat menghapus department yang memiliki employee. " +
                    "Silakan reassign employee ke department lain terlebih dahulu.");
        }

        department.setDeletedAt(java.time.LocalDateTime.now());
        departmentRepository.save(department);

        log.info("Department deleted successfully: {}", id);
    }

    // =====================================================
    // HIERARCHY METHODS
    // =====================================================

    /**
     * Get all root departments (departments without parent)
     */
    public List<Department> getRootDepartments() {
        return departmentRepository.findRootDepartments();
    }

    /**
     * Get child departments by parent ID
     */
    public List<Department> getChildDepartments(Long parentId) {
        return departmentRepository.findByParentIdAndDeletedAtIsNull(parentId);
    }

    /**
     * Get department tree as nested structure
     * @Transactional ensures the Hibernate session remains open while loading the tree
     */
    @Transactional(readOnly = true)
    public List<Department> getDepartmentTree() {
        List<Department> roots = getRootDepartments();
        for (Department root : roots) {
            loadChildren(root);
        }
        return roots;
    }

    /**
     * Get parent chain for a department
     * @Transactional ensures all parent relationships are loaded within the same session
     */
    @Transactional(readOnly = true)
    public List<Department> getParentChain(Long departmentId) {
        List<Department> chain = new ArrayList<>();

        // Start with the current department (with parent eagerly fetched)
        Department current = getDepartmentWithHeadAndParent(departmentId);
        if (current == null) {
            return chain;
        }

        // Traverse up to root, loading each parent within the transaction
        while (current != null) {
            chain.add(0, current); // Add to beginning

            // Get the parent ID from the eagerly-fetched parent
            Department parent = current.getParent();
            if (parent != null) {
                // Load parent with its parent eagerly fetched
                current = getDepartmentWithHeadAndParent(parent.getId());
            } else {
                current = null;
            }
        }

        return chain;
    }

    /**
     * Set department head
     */
    @Transactional
    public void setDepartmentHead(Long departmentId, Long headId) {
        log.info("Setting department head - Dept ID: {}, Head ID: {}", departmentId, headId);

        Department department = getDepartmentById(departmentId);
        if (department == null) {
            throw new IllegalArgumentException("Department not found");
        }

        Employee head = employeeRepository.findById(headId)
                .filter(emp -> emp.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        department.setHead(head);
        departmentRepository.save(department);

        log.info("Department head set successfully");
    }

    // =====================================================
    // VALIDATION METHODS
    // =====================================================

    /**
     * Check if department has children
     */
    public boolean hasChildren(Long departmentId) {
        List<Department> children = getChildDepartments(departmentId);
        return children != null && !children.isEmpty();
    }

    /**
     * Check if department has employees
     */
    public boolean hasEmployees(Long departmentId) {
        List<Employee> employees = employeeRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId);
        return employees != null && !employees.isEmpty();
    }

    /**
     * Check if setting parent would create circular reference
     */
    private boolean wouldCreateCircularReference(Department potentialParent, Department department) {
        Department current = potentialParent;
        while (current != null) {
            if (current.getId().equals(department.getId())) {
                return true; // Circular reference detected
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Recursively load children for tree view
     * Note: Children collections are initialized here to prevent LazyInitializationException
     */
    private void loadChildren(Department department) {
        List<Department> children = getChildDepartments(department.getId());
        department.setChildren(children);

        // Force initialization of the children collection while session is active
        // This prevents LazyInitializationException when the template accesses the collection
        if (department.getChildren() != null) {
            department.getChildren().size(); // Touch to initialize

            for (Department child : children) {
                // Recursively load grandchildren
                loadChildren(child);

                // Also initialize grandchildren collection for this child
                if (child.getChildren() != null) {
                    child.getChildren().size(); // Touch to initialize
                }
            }
        }
    }

    /**
     * Get department as DTO for API response
     */
    public DepartmentDto toDto(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setIsProdi(department.getIsProdi());
        dto.setKodeProdi(department.getKodeProdi());

        // Note: Level is not set here to avoid LazyInitializationException
        // The level can be calculated from parentChain in the controller if needed
        // For now, set to null as it's not critical for most views
        dto.setLevel(null);

        // Note: Parent and Head info are NOT set here to avoid LazyInitializationException
        // These should be set separately by the controller using the parentChain parameter
        // or by calling specific service methods to fetch this data safely

        // Children count - always use service method to avoid lazy initialization issues
        dto.setChildrenCount((long) getChildDepartments(department.getId()).size());

        // Employee count
        dto.setEmployeeCount((long) employeeRepository.findByDepartmentIdAndDeletedAtIsNull(department.getId()).size());

        return dto;
    }

    /**
     * Get departments as DTO list
     */
    public List<DepartmentDto> getDepartmentsAsDto() {
        return getAllDepartments().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get department tree as DTO list
     */
    public List<DepartmentDto> getDepartmentTreeAsDto() {
        return getDepartmentTree().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // =====================================================
    // DTO CLASSES
    // =====================================================

    public static class DepartmentDto {
        private Long id;
        private String name;
        private String description;
        private Boolean isProdi;
        private String kodeProdi;
        private Integer level;
        private Long parentId;
        private String parentName;
        private Long headId;
        private String headName;
        private Long childrenCount;
        private Long employeeCount;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Boolean getIsProdi() { return isProdi; }
        public void setIsProdi(Boolean isProdi) { this.isProdi = isProdi; }

        public String getKodeProdi() { return kodeProdi; }
        public void setKodeProdi(String kodeProdi) { this.kodeProdi = kodeProdi; }

        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }

        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }

        public String getParentName() { return parentName; }
        public void setParentName(String parentName) { this.parentName = parentName; }

        public Long getHeadId() { return headId; }
        public void setHeadId(Long headId) { this.headId = headId; }

        public String getHeadName() { return headName; }
        public void setHeadName(String headName) { this.headName = headName; }

        public Long getChildrenCount() { return childrenCount; }
        public void setChildrenCount(Long childrenCount) { this.childrenCount = childrenCount; }

        public Long getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(Long employeeCount) { this.employeeCount = employeeCount; }
    }
}
