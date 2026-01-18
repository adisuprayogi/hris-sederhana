package com.hris.model;

import com.hris.model.enums.LecturerRank;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Master table for lecturer salary rates based on academic rank
 * Stores all configurable rates for lecturer salary calculation
 */
@Entity
@Table(name = "lecturer_salary_rates")
public class LecturerSalaryRate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_rank", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private LecturerRank academicRank;

    // Basic Salary & Allowances for Permanent Lecturers
    @Column(name = "basic_salary", precision = 15, scale = 2)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(name = "functional_allowance", precision = 15, scale = 2)
    private BigDecimal functionalAllowance = BigDecimal.ZERO;

    // Teaching Rates for Contract Lecturers (per SKS)
    @Column(name = "contract_sks_rate_offline", precision = 15, scale = 2)
    private BigDecimal contractSksRateOffline = BigDecimal.ZERO;

    @Column(name = "contract_sks_rate_online", precision = 15, scale = 2)
    private BigDecimal contractSksRateOnline = BigDecimal.ZERO;

    // Overtime Teaching Rates for Permanent Lecturers (beyond obligation)
    @Column(name = "overtime_sks_rate_offline", precision = 15, scale = 2)
    private BigDecimal overtimeSksRateOffline = BigDecimal.ZERO;

    @Column(name = "overtime_sks_rate_online", precision = 15, scale = 2)
    private BigDecimal overtimeSksRateOnline = BigDecimal.ZERO;

    // Thesis Guidance & Examination Rates (per student/session)
    @Column(name = "thesis_guidance_rate_offline", precision = 15, scale = 2)
    private BigDecimal thesisGuidanceRateOffline = BigDecimal.ZERO;

    @Column(name = "thesis_guidance_rate_online", precision = 15, scale = 2)
    private BigDecimal thesisGuidanceRateOnline = BigDecimal.ZERO;

    @Column(name = "thesis_examination_rate_offline", precision = 15, scale = 2)
    private BigDecimal thesisExaminationRateOffline = BigDecimal.ZERO;

    @Column(name = "thesis_examination_rate_online", precision = 15, scale = 2)
    private BigDecimal thesisExaminationRateOnline = BigDecimal.ZERO;

    // Research & Publication Honors
    @Column(name = "research_honor", precision = 15, scale = 2)
    private BigDecimal researchHonor = BigDecimal.ZERO;

    @Column(name = "publication_honor", precision = 15, scale = 2)
    private BigDecimal publicationHonor = BigDecimal.ZERO;

    // Constructors
    public LecturerSalaryRate() {}

    public LecturerSalaryRate(LecturerRank academicRank) {
        this.academicRank = academicRank;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LecturerRank getAcademicRank() {
        return academicRank;
    }

    public void setAcademicRank(LecturerRank academicRank) {
        this.academicRank = academicRank;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary != null ? basicSalary : BigDecimal.ZERO;
    }

    public BigDecimal getFunctionalAllowance() {
        return functionalAllowance;
    }

    public void setFunctionalAllowance(BigDecimal functionalAllowance) {
        this.functionalAllowance = functionalAllowance != null ? functionalAllowance : BigDecimal.ZERO;
    }

    public BigDecimal getContractSksRateOffline() {
        return contractSksRateOffline;
    }

    public void setContractSksRateOffline(BigDecimal contractSksRateOffline) {
        this.contractSksRateOffline = contractSksRateOffline != null ? contractSksRateOffline : BigDecimal.ZERO;
    }

    public BigDecimal getContractSksRateOnline() {
        return contractSksRateOnline;
    }

    public void setContractSksRateOnline(BigDecimal contractSksRateOnline) {
        this.contractSksRateOnline = contractSksRateOnline != null ? contractSksRateOnline : BigDecimal.ZERO;
    }

    public BigDecimal getOvertimeSksRateOffline() {
        return overtimeSksRateOffline;
    }

    public void setOvertimeSksRateOffline(BigDecimal overtimeSksRateOffline) {
        this.overtimeSksRateOffline = overtimeSksRateOffline != null ? overtimeSksRateOffline : BigDecimal.ZERO;
    }

    public BigDecimal getOvertimeSksRateOnline() {
        return overtimeSksRateOnline;
    }

    public void setOvertimeSksRateOnline(BigDecimal overtimeSksRateOnline) {
        this.overtimeSksRateOnline = overtimeSksRateOnline != null ? overtimeSksRateOnline : BigDecimal.ZERO;
    }

    public BigDecimal getThesisGuidanceRateOffline() {
        return thesisGuidanceRateOffline;
    }

    public void setThesisGuidanceRateOffline(BigDecimal thesisGuidanceRateOffline) {
        this.thesisGuidanceRateOffline = thesisGuidanceRateOffline != null ? thesisGuidanceRateOffline : BigDecimal.ZERO;
    }

    public BigDecimal getThesisGuidanceRateOnline() {
        return thesisGuidanceRateOnline;
    }

    public void setThesisGuidanceRateOnline(BigDecimal thesisGuidanceRateOnline) {
        this.thesisGuidanceRateOnline = thesisGuidanceRateOnline != null ? thesisGuidanceRateOnline : BigDecimal.ZERO;
    }

    public BigDecimal getThesisExaminationRateOffline() {
        return thesisExaminationRateOffline;
    }

    public void setThesisExaminationRateOffline(BigDecimal thesisExaminationRateOffline) {
        this.thesisExaminationRateOffline = thesisExaminationRateOffline != null ? thesisExaminationRateOffline : BigDecimal.ZERO;
    }

    public BigDecimal getThesisExaminationRateOnline() {
        return thesisExaminationRateOnline;
    }

    public void setThesisExaminationRateOnline(BigDecimal thesisExaminationRateOnline) {
        this.thesisExaminationRateOnline = thesisExaminationRateOnline != null ? thesisExaminationRateOnline : BigDecimal.ZERO;
    }

    public BigDecimal getResearchHonor() {
        return researchHonor;
    }

    public void setResearchHonor(BigDecimal researchHonor) {
        this.researchHonor = researchHonor != null ? researchHonor : BigDecimal.ZERO;
    }

    public BigDecimal getPublicationHonor() {
        return publicationHonor;
    }

    public void setPublicationHonor(BigDecimal publicationHonor) {
        this.publicationHonor = publicationHonor != null ? publicationHonor : BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LecturerSalaryRate that = (LecturerSalaryRate) o;
        return Objects.equals(academicRank, that.academicRank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(academicRank);
    }

    @Override
    public String toString() {
        return "LecturerSalaryRate{" +
                "id=" + id +
                ", academicRank=" + academicRank +
                ", basicSalary=" + basicSalary +
                ", functionalAllowance=" + functionalAllowance +
                '}';
    }
}
