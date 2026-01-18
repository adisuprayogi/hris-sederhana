package com.hris.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Staging table for teaching attendance from Siakad
 * For Contract Lecturers - Used to calculate actual SKS taught
 */
@Entity
@Table(name = "teaching_attendance_staging")
public class TeachingAttendanceStaging extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "course_code", nullable = false, length = 50)
    private String courseCode;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "sks", nullable = false, precision = 3, scale = 1)
    private BigDecimal sks;

    @Column(name = "teaching_mode", length = 20)
    private String teachingMode = "OFFLINE";

    @Column(name = "attendance_status", length = 20)
    private String attendanceStatus = "HADIR";

    @Column(name = "payroll_period_used", length = 10)
    private String payrollPeriodUsed;

    @Column(name = "sync_date")
    private LocalDateTime syncDate;

    // Constructors
    public TeachingAttendanceStaging() {}

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

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public BigDecimal getSks() {
        return sks;
    }

    public void setSks(BigDecimal sks) {
        this.sks = sks;
    }

    public String getTeachingMode() {
        return teachingMode;
    }

    public void setTeachingMode(String teachingMode) {
        this.teachingMode = teachingMode;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
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

    public boolean isPresent() {
        return "HADIR".equalsIgnoreCase(attendanceStatus);
    }

    public boolean isOnline() {
        return "ONLINE".equalsIgnoreCase(teachingMode);
    }

    public boolean isOffline() {
        return "OFFLINE".equalsIgnoreCase(teachingMode);
    }
}
