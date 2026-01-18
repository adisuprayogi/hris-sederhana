package com.hris.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Staging table for thesis examination from Siakad
 * For thesis examination honor calculation
 */
@Entity
@Table(name = "thesis_examination_staging")
public class ThesisExaminationStaging extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "thesis_id", length = 50)
    private String thesisId;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "thesis_type", length = 50)
    private String thesisType;

    @Column(name = "examination_role", length = 50)
    private String examinationRole;

    @Column(name = "examination_mode", length = 20)
    private String examinationMode = "OFFLINE";

    @Column(name = "examination_date")
    private LocalDate examinationDate;

    @Column(name = "payroll_period_used", length = 10)
    private String payrollPeriodUsed;

    @Column(name = "sync_date")
    private LocalDateTime syncDate;

    // Constructors
    public ThesisExaminationStaging() {}

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

    public String getThesisId() {
        return thesisId;
    }

    public void setThesisId(String thesisId) {
        this.thesisId = thesisId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getThesisType() {
        return thesisType;
    }

    public void setThesisType(String thesisType) {
        this.thesisType = thesisType;
    }

    public String getExaminationRole() {
        return examinationRole;
    }

    public void setExaminationRole(String examinationRole) {
        this.examinationRole = examinationRole;
    }

    public String getExaminationMode() {
        return examinationMode;
    }

    public void setExaminationMode(String examinationMode) {
        this.examinationMode = examinationMode;
    }

    public LocalDate getExaminationDate() {
        return examinationDate;
    }

    public void setExaminationDate(LocalDate examinationDate) {
        this.examinationDate = examinationDate;
    }

    public String getPayrollPeriodUsed() {
        return payrollPeriodUsed;
    }

    public void setPayrollPeriodUsed(String payrollPeriodUsed) {
        this.payrollPeriodUsed = payrollPeriodUsed;
    }

    public LocalDateTime getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(LocalDateTime syncDate) {
        this.syncDate = syncDate;
    }

    public boolean isProcessed() {
        return payrollPeriodUsed != null;
    }

    public boolean isOnline() {
        return "ONLINE".equalsIgnoreCase(examinationMode);
    }

    public boolean isOffline() {
        return "OFFLINE".equalsIgnoreCase(examinationMode);
    }

    public boolean isAdvisor() {
        return "PEMBIMBING".equalsIgnoreCase(examinationRole);
    }

    public boolean isExaminer() {
        return examinationRole != null && examinationRole.startsWith("PENGUJI");
    }
}
