package com.hris.model;

import com.hris.model.enums.LecturerEmploymentStatus;
import com.hris.model.enums.LecturerRank;
import com.hris.model.enums.LecturerSalaryStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Header table for lecturer salary per period
 * Stores the main salary record per lecturer per period
 */
@Entity
@Table(name = "lecturer_salaries")
public class LecturerSalary extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecturer_profile_id", nullable = false)
    private Long lecturerProfileId;

    @Column(name = "period", nullable = false, length = 10)
    private String period; // YYYY-MM format

    @Column(name = "lecturer_type", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private LecturerEmploymentStatus lecturerType;

    @Column(name = "academic_rank", length = 50, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private LecturerRank academicRank;

    // Basic Salary Components (for Permanent Lecturers)
    @Column(name = "basic_salary", precision = 15, scale = 2)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(name = "functional_allowance", precision = 15, scale = 2)
    private BigDecimal functionalAllowance = BigDecimal.ZERO;

    // Teaching Honor (can be for both types)
    @Column(name = "teaching_honor_offline", precision = 15, scale = 2)
    private BigDecimal teachingHonorOffline = BigDecimal.ZERO;

    @Column(name = "teaching_honor_online", precision = 15, scale = 2)
    private BigDecimal teachingHonorOnline = BigDecimal.ZERO;

    @Column(name = "total_teaching_honor", precision = 15, scale = 2)
    private BigDecimal totalTeachingHonor = BigDecimal.ZERO;

    // Thesis Guidance & Examination Honors
    @Column(name = "thesis_guidance_honor_offline", precision = 15, scale = 2)
    private BigDecimal thesisGuidanceHonorOffline = BigDecimal.ZERO;

    @Column(name = "thesis_guidance_honor_online", precision = 15, scale = 2)
    private BigDecimal thesisGuidanceHonorOnline = BigDecimal.ZERO;

    @Column(name = "thesis_examination_honor_offline", precision = 15, scale = 2)
    private BigDecimal thesisExaminationHonorOffline = BigDecimal.ZERO;

    @Column(name = "thesis_examination_honor_online", precision = 15, scale = 2)
    private BigDecimal thesisExaminationHonorOnline = BigDecimal.ZERO;

    // Research & Publication Honors
    @Column(name = "research_honor", precision = 15, scale = 2)
    private BigDecimal researchHonor = BigDecimal.ZERO;

    @Column(name = "publication_honor", precision = 15, scale = 2)
    private BigDecimal publicationHonor = BigDecimal.ZERO;

    // Other Allowances
    @Column(name = "other_allowances", precision = 15, scale = 2)
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    // Summary
    @Column(name = "total_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSalary = BigDecimal.ZERO;

    // Processing Status
    @Column(name = "status", length = 50, columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private LecturerSalaryStatus status = LecturerSalaryStatus.DRAFT;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Payment Info
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // Relationships
    @OneToMany(mappedBy = "lecturerSalary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LecturerSalaryDetail> details = new ArrayList<>();

    // Constructors
    public LecturerSalary() {}

    public LecturerSalary(Long lecturerProfileId, String period, LecturerEmploymentStatus lecturerType) {
        this.lecturerProfileId = lecturerProfileId;
        this.period = period;
        this.lecturerType = lecturerType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLecturerProfileId() {
        return lecturerProfileId;
    }

    public void setLecturerProfileId(Long lecturerProfileId) {
        this.lecturerProfileId = lecturerProfileId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LecturerEmploymentStatus getLecturerType() {
        return lecturerType;
    }

    public void setLecturerType(LecturerEmploymentStatus lecturerType) {
        this.lecturerType = lecturerType;
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
        this.basicSalary = safeBigDecimal(basicSalary);
    }

    public BigDecimal getFunctionalAllowance() {
        return functionalAllowance;
    }

    public void setFunctionalAllowance(BigDecimal functionalAllowance) {
        this.functionalAllowance = safeBigDecimal(functionalAllowance);
    }

    public BigDecimal getTeachingHonorOffline() {
        return teachingHonorOffline;
    }

    public void setTeachingHonorOffline(BigDecimal teachingHonorOffline) {
        this.teachingHonorOffline = safeBigDecimal(teachingHonorOffline);
    }

    public BigDecimal getTeachingHonorOnline() {
        return teachingHonorOnline;
    }

    public void setTeachingHonorOnline(BigDecimal teachingHonorOnline) {
        this.teachingHonorOnline = safeBigDecimal(teachingHonorOnline);
    }

    public BigDecimal getTotalTeachingHonor() {
        return totalTeachingHonor;
    }

    public void setTotalTeachingHonor(BigDecimal totalTeachingHonor) {
        this.totalTeachingHonor = safeBigDecimal(totalTeachingHonor);
    }

    public BigDecimal getThesisGuidanceHonorOffline() {
        return thesisGuidanceHonorOffline;
    }

    public void setThesisGuidanceHonorOffline(BigDecimal thesisGuidanceHonorOffline) {
        this.thesisGuidanceHonorOffline = safeBigDecimal(thesisGuidanceHonorOffline);
    }

    public BigDecimal getThesisGuidanceHonorOnline() {
        return thesisGuidanceHonorOnline;
    }

    public void setThesisGuidanceHonorOnline(BigDecimal thesisGuidanceHonorOnline) {
        this.thesisGuidanceHonorOnline = safeBigDecimal(thesisGuidanceHonorOnline);
    }

    public BigDecimal getThesisExaminationHonorOffline() {
        return thesisExaminationHonorOffline;
    }

    public void setThesisExaminationHonorOffline(BigDecimal thesisExaminationHonorOffline) {
        this.thesisExaminationHonorOffline = safeBigDecimal(thesisExaminationHonorOffline);
    }

    public BigDecimal getThesisExaminationHonorOnline() {
        return thesisExaminationHonorOnline;
    }

    public void setThesisExaminationHonorOnline(BigDecimal thesisExaminationHonorOnline) {
        this.thesisExaminationHonorOnline = safeBigDecimal(thesisExaminationHonorOnline);
    }

    public BigDecimal getResearchHonor() {
        return researchHonor;
    }

    public void setResearchHonor(BigDecimal researchHonor) {
        this.researchHonor = safeBigDecimal(researchHonor);
    }

    public BigDecimal getPublicationHonor() {
        return publicationHonor;
    }

    public void setPublicationHonor(BigDecimal publicationHonor) {
        this.publicationHonor = safeBigDecimal(publicationHonor);
    }

    public BigDecimal getOtherAllowances() {
        return otherAllowances;
    }

    public void setOtherAllowances(BigDecimal otherAllowances) {
        this.otherAllowances = safeBigDecimal(otherAllowances);
    }

    public BigDecimal getTotalSalary() {
        return totalSalary;
    }

    public void setTotalSalary(BigDecimal totalSalary) {
        this.totalSalary = safeBigDecimal(totalSalary);
    }

    public LecturerSalaryStatus getStatus() {
        return status;
    }

    public void setStatus(LecturerSalaryStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public List<LecturerSalaryDetail> getDetails() {
        return details;
    }

    public void setDetails(List<LecturerSalaryDetail> details) {
        this.details = details;
    }

    public void addDetail(LecturerSalaryDetail detail) {
        details.add(detail);
        detail.setLecturerSalary(this);
    }

    public void removeDetail(LecturerSalaryDetail detail) {
        details.remove(detail);
        detail.setLecturerSalary(null);
    }

    // Helper methods
    public BigDecimal getTotalThesisGuidanceHonor() {
        return thesisGuidanceHonorOffline.add(thesisGuidanceHonorOnline);
    }

    public BigDecimal getTotalThesisExaminationHonor() {
        return thesisExaminationHonorOffline.add(thesisExaminationHonorOnline);
    }

    public boolean isPermanent() {
        return lecturerType == LecturerEmploymentStatus.DOSEN_TETAP;
    }

    public boolean isContract() {
        return lecturerType == LecturerEmploymentStatus.DOSEN_TIDAK_TETAP;
    }

    public boolean isDraft() {
        return status == LecturerSalaryStatus.DRAFT;
    }

    public boolean isCalculated() {
        return status == LecturerSalaryStatus.CALCULATED;
    }

    public boolean isPaid() {
        return status == LecturerSalaryStatus.PAID;
    }

    public void markAsCalculated() {
        this.status = LecturerSalaryStatus.CALCULATED;
        this.calculatedAt = LocalDateTime.now();
    }

    public void markAsPaid() {
        this.status = LecturerSalaryStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void calculateTotal() {
        this.totalTeachingHonor = teachingHonorOffline.add(teachingHonorOnline);
        this.totalSalary = basicSalary
                .add(functionalAllowance)
                .add(totalTeachingHonor)
                .add(thesisGuidanceHonorOffline)
                .add(thesisGuidanceHonorOnline)
                .add(thesisExaminationHonorOffline)
                .add(thesisExaminationHonorOnline)
                .add(researchHonor)
                .add(publicationHonor)
                .add(otherAllowances);
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
