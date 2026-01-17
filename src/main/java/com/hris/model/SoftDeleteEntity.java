package com.hris.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

/**
 * Soft Delete Entity Class
 * Provides soft delete functionality for entities
 */
@MappedSuperclass
@Getter
@Setter
@Filters({@org.hibernate.annotations.Filter(name = "deletedFilter", condition = "deleted_at is null")})
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * Soft delete this entity
     */
    public void softDelete(Long deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Restore this entity (undo soft delete)
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Check if this entity is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
