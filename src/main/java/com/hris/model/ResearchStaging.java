package com.hris.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Staging table for research data from Research System
 * For research honor calculation
 */
@Entity
@Table(name = "research_staging")
public class ResearchStaging extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "research_title")
    private String researchTitle;

    @Column(name = "research_type", length = 100)
    private String researchType;

    @Column(name = "research_tier", length = 50)
    private String researchTier;

    @Column(name = "research_duration_months")
    private Integer researchDurationMonths;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @Column(name = "payroll_period_used", length = 10)
    private String payrollPeriodUsed;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "sync_date")
    private LocalDateTime syncDate;

    // Constructors
    public ResearchStaging() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getResearchTitle() {
        return researchTitle;
    }

    public void setResearchTitle(String researchTitle) {
        this.researchTitle = researchTitle;
    }

    public String getResearchType() {
        return researchType;
    }

    public void setResearchType(String researchType) {
        this.researchType = researchType;
    }

    public String getResearchTier() {
        return researchTier;
    }

    public void setResearchTier(String researchTier) {
        this.researchTier = researchTier;
    }

    public Integer getResearchDurationMonths() {
        return researchDurationMonths;
    }

    public void setResearchDurationMonths(Integer researchDurationMonths) {
        this.researchDurationMonths = researchDurationMonths;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed != null ? isProcessed : false;
    }

    public boolean isProcessed() {
        return isProcessed != null && isProcessed;
    }

    public String getPayrollPeriodUsed() {
        return payrollPeriodUsed;
    }

    public void setPayrollPeriodUsed(String payrollPeriodUsed) {
        this.payrollPeriodUsed = payrollPeriodUsed;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public LocalDateTime getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(LocalDateTime syncDate) {
        this.syncDate = syncDate;
    }

    public boolean isInternational() {
        return "INTERNASIONAL".equalsIgnoreCase(researchTier);
    }

    public boolean isNational() {
        return "NASIONAL".equalsIgnoreCase(researchTier);
    }

    public boolean isInstitutional() {
        return "LEMBAGA".equalsIgnoreCase(researchTier);
    }
}
