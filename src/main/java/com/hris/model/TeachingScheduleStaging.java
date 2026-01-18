package com.hris.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Staging table for teaching schedule from Siakad
 * For Permanent Lecturers - Used to calculate teaching obligation & overtime
 */
@Entity
@Table(name = "teaching_schedule_staging")
public class TeachingScheduleStaging extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Column(name = "semester", nullable = false, length = 20)
    private String semester;

    @Column(name = "course_code", nullable = false, length = 50)
    private String courseCode;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "schedule_day", length = 20)
    private String scheduleDay;

    @Column(name = "schedule_time", length = 50)
    private String scheduleTime;

    @Column(name = "sks", nullable = false, precision = 3, scale = 1)
    private BigDecimal sks;

    @Column(name = "teaching_mode", length = 20)
    private String teachingMode = "OFFLINE";

    @Column(name = "payroll_period_used", length = 10)
    private String payrollPeriodUsed;

    @Column(name = "sync_date")
    private LocalDateTime syncDate;

    // Constructors
    public TeachingScheduleStaging() {}

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

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
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

    public String getScheduleDay() {
        return scheduleDay;
    }

    public void setScheduleDay(String scheduleDay) {
        this.scheduleDay = scheduleDay;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
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
        return "ONLINE".equalsIgnoreCase(teachingMode);
    }

    public boolean isOffline() {
        return "OFFLINE".equalsIgnoreCase(teachingMode);
    }
}
