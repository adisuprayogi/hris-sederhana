package com.hris.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Department Entity
 * Represents a department or study program (prodi)
 * Supports hierarchical structure with parent/child relationships
 */
@Entity
@Table(name = "departments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Department extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nama department wajib diisi")
    @Size(max = 100, message = "Nama department maksimal 100 karakter")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Deskripsi maksimal 500 karakter")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_prodi")
    private Boolean isProdi = false;

    @Pattern(regexp = "^[A-Z0-9]{3,10}$", message = "Kode prodi harus 3-10 karakter huruf kapital dan angka")
    @Column(name = "kode_prodi", length = 20)
    private String kodeProdi;

    // =====================================================
    // APPROVAL STRUCTURE FIELDS
    // =====================================================

    /**
     * Parent department for hierarchical structure
     * Self-referencing relationship
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    /**
     * Head of this department
     * An employee who is responsible for this department
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id")
    private Employee head;

    /**
     * Child departments
     * Inverse side of parent relationship
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Department> children = new ArrayList<>();

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Get the full chain of parent departments from root to this department
     * Example: [Universitas, WAREK 1, Fakultas Teknik, Department IT]
     *
     * @return List of departments from root to this department
     */
    public List<Department> getParentChain() {
        List<Department> chain = new ArrayList<>();
        Department current = this;

        // Traverse up to root
        while (current != null) {
            chain.add(0, current); // Add to beginning
            current = current.getParent();
        }

        return chain;
    }

    /**
     * Get the level of this department in the hierarchy
     * Root = 0, direct child = 1, grandchild = 2, etc.
     *
     * @return The depth level in the hierarchy
     */
    public int getLevel() {
        int level = 0;
        Department current = this.getParent();

        while (current != null) {
            level++;
            current = current.getParent();
        }

        return level;
    }

    /**
     * Check if this department is a root department (no parent)
     *
     * @return true if this is a root department
     */
    public boolean isRoot() {
        return this.getParent() == null;
    }

    /**
     * Check if this department has children
     *
     * @return true if this department has child departments
     */
    public boolean hasChildren() {
        return this.getChildren() != null && !this.getChildren().isEmpty();
    }

    /**
     * Add a child department to this department
     *
     * @param child The child department to add
     */
    public void addChild(Department child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParent(this);
    }

    /**
     * Remove a child department from this department
     *
     * @param child The child department to remove
     */
    public void removeChild(Department child) {
        if (this.children != null) {
            this.children.remove(child);
            child.setParent(null);
        }
    }
}
