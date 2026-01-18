package com.hris.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Staging table for publication data from Publication System
 * For publication honor calculation with SINTA & Scopus classification
 */
@Entity
@Table(name = "publication_staging")
public class PublicationStaging extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "publication_title")
    private String publicationTitle;

    @Column(name = "publication_type", length = 50)
    private String publicationType;

    // SINTA Classification (National)
    @Column(name = "sinta_level", length = 20)
    private String sintaLevel;

    // Scopus Classification (International)
    @Column(name = "is_scopus_indexed")
    private Boolean isScopusIndexed = false;

    @Column(name = "scopus_quartile", length = 10)
    private String scopusQuartile;

    @Column(name = "journal_name")
    private String journalName;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "doi")
    private String doi;

    @Column(name = "authors", columnDefinition = "TEXT")
    private String authors;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @Column(name = "payroll_period_used", length = 10)
    private String payrollPeriodUsed;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "sync_date")
    private LocalDateTime syncDate;

    // Enums for classification
    public enum SintaLevel {
        SINTA_1, SINTA_2, SINTA_3, SINTA_4, SINTA_5, SINTA_6, NONE
    }

    public enum ScopusQuartile {
        Q1, Q2, Q3, Q4, NONE
    }

    // Constructors
    public PublicationStaging() {}

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

    public String getPublicationTitle() {
        return publicationTitle;
    }

    public void setPublicationTitle(String publicationTitle) {
        this.publicationTitle = publicationTitle;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getSintaLevel() {
        return sintaLevel;
    }

    public void setSintaLevel(String sintaLevel) {
        this.sintaLevel = sintaLevel;
    }

    public Boolean getIsScopusIndexed() {
        return isScopusIndexed;
    }

    public void setIsScopusIndexed(Boolean isScopusIndexed) {
        this.isScopusIndexed = isScopusIndexed != null ? isScopusIndexed : false;
    }

    public boolean isScopusIndexed() {
        return isScopusIndexed != null && isScopusIndexed;
    }

    public String getScopusQuartile() {
        return scopusQuartile;
    }

    public void setScopusQuartile(String scopusQuartile) {
        this.scopusQuartile = scopusQuartile;
    }

    public String getJournalName() {
        return journalName;
    }

    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
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

    // Helper methods for classification
    public boolean isSintaIndexed() {
        return sintaLevel != null && !sintaLevel.equals("NONE");
    }

    public int getSintaLevelNumeric() {
        if (sintaLevel == null || sintaLevel.equals("NONE")) {
            return 0;
        }
        try {
            return Integer.parseInt(sintaLevel.replace("SINTA_", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isTopTierSinta() {
        int level = getSintaLevelNumeric();
        return level >= 1 && level <= 2;
    }

    public boolean isMidTierSinta() {
        int level = getSintaLevelNumeric();
        return level >= 3 && level <= 4;
    }

    public boolean isTopTierScopus() {
        return "Q1".equals(scopusQuartile);
    }

    public boolean isMidTierScopus() {
        return "Q2".equals(scopusQuartile);
    }
}
