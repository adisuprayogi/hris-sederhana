package com.hris.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Auditable Entity Class
 * Provides full audit trail fields including created_by and updated_by
 * Extends SoftDeleteEntity to include soft delete functionality
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity extends SoftDeleteEntity {

    @Column(name = "created_by", insertable = true, updatable = false)
    private Long createdBy;

    @Column(name = "updated_by", insertable = true, updatable = true)
    private Long updatedBy;
}
