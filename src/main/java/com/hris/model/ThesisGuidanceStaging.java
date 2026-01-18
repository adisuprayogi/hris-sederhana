package com.hris.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Staging table for thesis guidance from Siakad
 * For thesis/supervision honor calculation
 */
@Entity
@Table(name = "thesis_guidance_staging")
public class ThesisGuidanceStaging extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "thesis_type", length = 50)
    private String thesisType;

    @Column(name = "guidance_mode", length = 20)
    private String guidanceMode = "OFFLINE";

    @Column(name = "guidance_session")
    private Integer guidanceSession;

    @Column(name = "payroll_period_used", length = 10)
    private String payrollPeriodUsed;

    @Column(name = "sync_date")
    private LocalDateTime syncDate;

    // Constructors
    public ThesisGuidanceStaging() {}

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

    public String getGuidanceMode() {
        return guidanceMode;
    }

    public void setGuidanceMode(String guidanceMode) {
        this.guidanceMode = guidanceMode;
    }

    public Integer getGuidanceSession() {
        return guidanceSession;
    }

    public void setGuidanceSession(Integer guidanceSession) {
        this.guidanceSession = guidanceSession;
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
        return "ONLINE".equalsIgnoreCase(guidanceMode);
    }

    public boolean isOffline() {
        return "OFFLINE".equalsIgnoreCase(guidanceMode);
    }
}
