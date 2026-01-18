package com.hris.model;

import com.hris.model.enums.SalaryComponentType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Detail table for lecturer salary components
 * Stores the breakdown of each salary component with audit trail
 */
@Entity
@Table(name = "lecturer_salary_details")
public class LecturerSalaryDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_salary_id", nullable = false)
    private LecturerSalary lecturerSalary;

    // Component Classification
    @Column(name = "component_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private SalaryComponentType componentType;

    @Column(name = "component_code", length = 50)
    private String componentCode;

    @Column(name = "description", nullable = false)
    private String description;

    // Calculation Details
    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "rate", precision = 15, scale = 2)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    // Source Reference (for audit trail)
    @Column(name = "source_reference_id")
    private Long sourceReferenceId;

    @Column(name = "source_table", length = 100)
    private String sourceTable;

    // Constructors
    public LecturerSalaryDetail() {}

    public LecturerSalaryDetail(SalaryComponentType componentType, String description,
                                BigDecimal quantity, BigDecimal rate, BigDecimal amount) {
        this.componentType = componentType;
        this.description = description;
        this.quantity = quantity != null ? quantity : BigDecimal.ZERO;
        this.rate = rate != null ? rate : BigDecimal.ZERO;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LecturerSalary getLecturerSalary() {
        return lecturerSalary;
    }

    public void setLecturerSalary(LecturerSalary lecturerSalary) {
        this.lecturerSalary = lecturerSalary;
    }

    public SalaryComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(SalaryComponentType componentType) {
        this.componentType = componentType;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity != null ? quantity : BigDecimal.ZERO;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate != null ? rate : BigDecimal.ZERO;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public Long getSourceReferenceId() {
        return sourceReferenceId;
    }

    public void setSourceReferenceId(Long sourceReferenceId) {
        this.sourceReferenceId = sourceReferenceId;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    // Helper methods
    public void calculateAmount() {
        if (quantity != null && rate != null) {
            this.amount = quantity.multiply(rate);
        }
    }

    public boolean hasSourceReference() {
        return sourceReferenceId != null && sourceTable != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LecturerSalaryDetail that = (LecturerSalaryDetail) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LecturerSalaryDetail{" +
                "id=" + id +
                ", componentType=" + componentType +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", rate=" + rate +
                ", amount=" + amount +
                '}';
    }
}
