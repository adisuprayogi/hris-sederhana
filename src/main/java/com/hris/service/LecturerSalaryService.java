package com.hris.service;

import com.hris.model.*;
import com.hris.model.enums.*;
import com.hris.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LecturerSalaryService {

    @Autowired
    private LecturerSalaryRepository salaryRepository;

    @Autowired
    private LecturerSalaryRateService rateService;

    @Autowired
    private LecturerProfileService lecturerProfileService;

    @Autowired
    private TeachingScheduleStagingRepository teachingScheduleStagingRepository;

    @Autowired
    private TeachingAttendanceStagingRepository teachingAttendanceStagingRepository;

    @Autowired
    private ThesisGuidanceStagingRepository thesisGuidanceStagingRepository;

    @Autowired
    private ThesisExaminationStagingRepository thesisExaminationStagingRepository;

    @Autowired
    private ResearchStagingRepository researchStagingRepository;

    @Autowired
    private PublicationStagingRepository publicationStagingRepository;

    // Teaching obligation SKS for permanent lecturers
    private static final BigDecimal TEACHING_OBLIGATION_WITH_POSITION = new BigDecimal("3"); // With structural position
    private static final BigDecimal TEACHING_OBLIGATION_WITHOUT_POSITION = new BigDecimal("12"); // Without structural position

    public List<LecturerSalary> getAllSalaries() {
        return salaryRepository.findAllByDeletedAtIsNullOrderByPeriodDesc();
    }

    public List<LecturerSalary> getSalariesByPeriod(String period) {
        return salaryRepository.findByPeriodAndDeletedAtIsNullOrderByLecturerProfileId(period);
    }

    public List<LecturerSalary> getSalariesByLecturer(Long lecturerId) {
        return salaryRepository.findByLecturerProfileIdAndDeletedAtIsNullOrderByPeriodDesc(lecturerId);
    }

    public List<LecturerSalary> getSalariesByPeriodAndLecturerEmploymentStatus(String period, LecturerEmploymentStatus lecturerType) {
        return salaryRepository.findByPeriodAndLecturerTypeAndDeletedAtIsNull(period, lecturerType);
    }

    public LecturerSalary getSalaryById(Long id) {
        return salaryRepository.findById(id).orElse(null);
    }

    /**
     * Calculate salary for a permanent lecturer for a specific period
     * Includes: Basic Salary + Functional Allowance + Overtime Teaching + Thesis Guidance/Examination + Research + Publication
     */
    @Transactional
    public LecturerSalary calculatePermanentLecturerSalary(Long lecturerProfileId, String period) {
        LecturerProfile profile = lecturerProfileService.getById(lecturerProfileId);
        if (profile == null) {
            throw new IllegalArgumentException("Lecturer profile not found");
        }

        if (profile.getEmploymentStatus() != LecturerEmploymentStatus.DOSEN_TETAP) {
            throw new IllegalArgumentException("Lecturer is not a permanent lecturer");
        }

        // Check if salary already exists for this period
        if (salaryRepository.existsByLecturerProfileIdAndPeriodAndDeletedAtIsNull(lecturerProfileId, period)) {
            throw new IllegalArgumentException("Salary already calculated for this period");
        }

        LecturerRank rank = profile.getLecturerRank();
        if (rank == null) {
            throw new IllegalArgumentException("Academic rank not set for lecturer");
        }

        LecturerSalary salary = new LecturerSalary(lecturerProfileId, period, LecturerEmploymentStatus.DOSEN_TETAP);
        salary.setAcademicRank(rank);

        // Basic Salary & Functional Allowance
        salary.setBasicSalary(rateService.getBasicSalary(rank));
        salary.setFunctionalAllowance(rateService.getFunctionalAllowance(rank));

        // Calculate overtime teaching honor
        calculateOvertimeTeachingHonor(salary, lecturerProfileId, period);

        // Calculate thesis guidance honor
        calculateThesisGuidanceHonor(salary, lecturerProfileId);

        // Calculate thesis examination honor
        calculateThesisExaminationHonor(salary, lecturerProfileId);

        // Calculate research honor
        calculateResearchHonor(salary, lecturerProfileId);

        // Calculate publication honor
        calculatePublicationHonor(salary, lecturerProfileId);

        // Calculate total
        salary.calculateTotal();
        salary.markAsCalculated();

        LecturerSalary saved = salaryRepository.save(salary);

        // Mark staging data as processed
        markStagingDataAsProcessed(lecturerProfileId, period, LecturerEmploymentStatus.DOSEN_TETAP);

        return saved;
    }

    /**
     * Calculate salary for a contract lecturer for a specific period
     * Includes: Teaching (per SKS) + Thesis Guidance/Examination + Research + Publication
     */
    @Transactional
    public LecturerSalary calculateContractLecturerSalary(Long lecturerProfileId, String period) {
        LecturerProfile profile = lecturerProfileService.getById(lecturerProfileId);
        if (profile == null) {
            throw new IllegalArgumentException("Lecturer profile not found");
        }

        if (profile.getEmploymentStatus() != LecturerEmploymentStatus.DOSEN_TIDAK_TETAP) {
            throw new IllegalArgumentException("Lecturer is not a contract lecturer");
        }

        // Check if salary already exists for this period
        if (salaryRepository.existsByLecturerProfileIdAndPeriodAndDeletedAtIsNull(lecturerProfileId, period)) {
            throw new IllegalArgumentException("Salary already calculated for this period");
        }

        LecturerRank rank = profile.getLecturerRank();
        if (rank == null) {
            throw new IllegalArgumentException("Academic rank not set for lecturer");
        }

        LecturerSalary salary = new LecturerSalary(lecturerProfileId, period, LecturerEmploymentStatus.DOSEN_TIDAK_TETAP);
        salary.setAcademicRank(rank);

        // Calculate teaching honor based on attendance (per SKS)
        calculateContractTeachingHonor(salary, lecturerProfileId, period);

        // Calculate thesis guidance honor
        calculateThesisGuidanceHonor(salary, lecturerProfileId);

        // Calculate thesis examination honor
        calculateThesisExaminationHonor(salary, lecturerProfileId);

        // Calculate research honor
        calculateResearchHonor(salary, lecturerProfileId);

        // Calculate publication honor
        calculatePublicationHonor(salary, lecturerProfileId);

        // Calculate total
        salary.calculateTotal();
        salary.markAsCalculated();

        LecturerSalary saved = salaryRepository.save(salary);

        // Mark staging data as processed
        markStagingDataAsProcessed(lecturerProfileId, period, LecturerEmploymentStatus.DOSEN_TIDAK_TETAP);

        return saved;
    }

    private void calculateOvertimeTeachingHonor(LecturerSalary salary, Long lecturerId, String period) {
        LecturerRank rank = salary.getAcademicRank();

        // Get teaching schedule for the period (academic year/semester)
        List<TeachingScheduleStaging> schedules = teachingScheduleStagingRepository
                .findUnprocessedByLecturerId(lecturerId);

        BigDecimal totalSks = BigDecimal.ZERO;
        BigDecimal offlineSks = BigDecimal.ZERO;
        BigDecimal onlineSks = BigDecimal.ZERO;

        for (TeachingScheduleStaging schedule : schedules) {
            BigDecimal sks = schedule.getSks();
            totalSks = totalSks.add(sks);

            if (schedule.isOnline()) {
                onlineSks = onlineSks.add(sks);
            } else {
                offlineSks = offlineSks.add(sks);
            }

            // Create detail record
            LecturerSalaryDetail detail = new LecturerSalaryDetail();
            detail.setComponentType(SalaryComponentType.TEACHING_OFFLINE);
            detail.setDescription(schedule.getCourseName() != null ? schedule.getCourseName() : schedule.getCourseCode());
            detail.setQuantity(sks);
            detail.setSourceReferenceId(schedule.getId());
            detail.setSourceTable("teaching_schedule_staging");
            salary.addDetail(detail);
        }

        // Calculate obligation (assuming with position for now - can be enhanced later)
        BigDecimal obligation = TEACHING_OBLIGATION_WITH_POSITION;
        BigDecimal overtimeSks = totalSks.subtract(obligation);

        if (overtimeSks.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate overtime honor
            BigDecimal offlineRate = rateService.getOvertimeSksRate(rank, false);
            BigDecimal onlineRate = rateService.getOvertimeSksRate(rank, true);

            salary.setTeachingHonorOffline(offlineSks.multiply(offlineRate));
            salary.setTeachingHonorOnline(onlineSks.multiply(onlineRate));

            // Update details with rates and amounts
            for (LecturerSalaryDetail detail : salary.getDetails()) {
                if (detail.getComponentType() == SalaryComponentType.TEACHING_OFFLINE) {
                    detail.setRate(offlineRate);
                    detail.calculateAmount();
                } else if (detail.getComponentType() == SalaryComponentType.TEACHING_ONLINE) {
                    detail.setRate(onlineRate);
                    detail.calculateAmount();
                }
            }
        }
    }

    private void calculateContractTeachingHonor(LecturerSalary salary, Long lecturerId, String period) {
        // Parse period to get date range (YYYY-MM)
        String[] parts = period.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        // Get first and last day of the month
        java.time.LocalDate startDate = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        LecturerRank rank = salary.getAcademicRank();

        // Get teaching attendance for the period
        List<TeachingAttendanceStaging> attendances = teachingAttendanceStagingRepository
                .findPresentAttendanceByLecturerAndDateRange(lecturerId, startDate, endDate);

        BigDecimal offlineSks = BigDecimal.ZERO;
        BigDecimal onlineSks = BigDecimal.ZERO;

        for (TeachingAttendanceStaging attendance : attendances) {
            BigDecimal sks = attendance.getSks();

            LecturerSalaryDetail detail = new LecturerSalaryDetail();
            detail.setDescription(attendance.getCourseName() != null ? attendance.getCourseName() : attendance.getCourseCode());
            detail.setQuantity(sks);
            detail.setSourceReferenceId(attendance.getId());
            detail.setSourceTable("teaching_attendance_staging");

            if (attendance.isOnline()) {
                detail.setComponentType(SalaryComponentType.TEACHING_ONLINE);
                onlineSks = onlineSks.add(sks);
            } else {
                detail.setComponentType(SalaryComponentType.TEACHING_OFFLINE);
                offlineSks = offlineSks.add(sks);
            }

            salary.addDetail(detail);
        }

        // Calculate honor
        BigDecimal offlineRate = rateService.getContractSksRate(rank, false);
        BigDecimal onlineRate = rateService.getContractSksRate(rank, true);

        salary.setTeachingHonorOffline(offlineSks.multiply(offlineRate));
        salary.setTeachingHonorOnline(onlineSks.multiply(onlineRate));

        // Update details with rates and amounts
        for (LecturerSalaryDetail detail : salary.getDetails()) {
            if (detail.getComponentType() == SalaryComponentType.TEACHING_OFFLINE) {
                detail.setRate(offlineRate);
                detail.calculateAmount();
            } else if (detail.getComponentType() == SalaryComponentType.TEACHING_ONLINE) {
                detail.setRate(onlineRate);
                detail.calculateAmount();
            }
        }
    }

    private void calculateThesisGuidanceHonor(LecturerSalary salary, Long lecturerId) {
        LecturerRank rank = salary.getAcademicRank();
        BigDecimal offlineRate = rateService.getThesisGuidanceRate(rank, false);
        BigDecimal onlineRate = rateService.getThesisGuidanceRate(rank, true);

        List<ThesisGuidanceStaging> guidanceList = thesisGuidanceStagingRepository
                .findUnprocessedByLecturerId(lecturerId);

        long offlineCount = 0;
        long onlineCount = 0;

        for (ThesisGuidanceStaging guidance : guidanceList) {
            LecturerSalaryDetail detail = new LecturerSalaryDetail();
            detail.setDescription("Bimbingan " + guidance.getThesisType() + " - " + guidance.getStudentName());
            detail.setQuantity(BigDecimal.ONE);
            detail.setSourceReferenceId(guidance.getId());
            detail.setSourceTable("thesis_guidance_staging");

            if (guidance.isOnline()) {
                detail.setComponentType(SalaryComponentType.GUIDANCE_ONLINE);
                detail.setRate(onlineRate);
                detail.calculateAmount();
                onlineCount++;
            } else {
                detail.setComponentType(SalaryComponentType.GUIDANCE_OFFLINE);
                detail.setRate(offlineRate);
                detail.calculateAmount();
                offlineCount++;
            }

            salary.addDetail(detail);
        }

        salary.setThesisGuidanceHonorOffline(new BigDecimal(offlineCount).multiply(offlineRate));
        salary.setThesisGuidanceHonorOnline(new BigDecimal(onlineCount).multiply(onlineRate));
    }

    private void calculateThesisExaminationHonor(LecturerSalary salary, Long lecturerId) {
        LecturerRank rank = salary.getAcademicRank();
        BigDecimal offlineRate = rateService.getThesisExaminationRate(rank, false);
        BigDecimal onlineRate = rateService.getThesisExaminationRate(rank, true);

        List<ThesisExaminationStaging> examinationList = thesisExaminationStagingRepository
                .findUnprocessedByLecturerId(lecturerId);

        long offlineCount = 0;
        long onlineCount = 0;

        for (ThesisExaminationStaging examination : examinationList) {
            LecturerSalaryDetail detail = new LecturerSalaryDetail();
            detail.setDescription("Menguji " + examination.getThesisType() + " - " +
                    examination.getStudentName() + " (" + examination.getExaminationRole() + ")");
            detail.setQuantity(BigDecimal.ONE);
            detail.setSourceReferenceId(examination.getId());
            detail.setSourceTable("thesis_examination_staging");

            if (examination.isOnline()) {
                detail.setComponentType(SalaryComponentType.EXAMINATION_ONLINE);
                detail.setRate(onlineRate);
                detail.calculateAmount();
                onlineCount++;
            } else {
                detail.setComponentType(SalaryComponentType.EXAMINATION_OFFLINE);
                detail.setRate(offlineRate);
                detail.calculateAmount();
                offlineCount++;
            }

            salary.addDetail(detail);
        }

        salary.setThesisExaminationHonorOffline(new BigDecimal(offlineCount).multiply(offlineRate));
        salary.setThesisExaminationHonorOnline(new BigDecimal(onlineCount).multiply(onlineRate));
    }

    private void calculateResearchHonor(LecturerSalary salary, Long lecturerId) {
        List<ResearchStaging> researchList = researchStagingRepository
                .findUnprocessedByLecturerId(lecturerId);

        LecturerRank rank = salary.getAcademicRank();
        BigDecimal researchHonor = rateService.getResearchHonor(rank);

        BigDecimal total = BigDecimal.ZERO;

        for (ResearchStaging research : researchList) {
            LecturerSalaryDetail detail = new LecturerSalaryDetail();
            detail.setComponentType(SalaryComponentType.RESEARCH);
            detail.setDescription("Penelitian: " + research.getResearchTitle());
            detail.setQuantity(BigDecimal.ONE);
            detail.setRate(researchHonor);
            detail.setAmount(researchHonor);
            detail.setSourceReferenceId(research.getId());
            detail.setSourceTable("research_staging");

            salary.addDetail(detail);
            total = total.add(researchHonor);
        }

        salary.setResearchHonor(total);
    }

    private void calculatePublicationHonor(LecturerSalary salary, Long lecturerId) {
        List<PublicationStaging> publicationList = publicationStagingRepository
                .findUnprocessedByLecturerId(lecturerId);

        LecturerRank rank = salary.getAcademicRank();
        BigDecimal baseHonor = rateService.getPublicationHonor(rank);

        BigDecimal total = BigDecimal.ZERO;

        for (PublicationStaging publication : publicationList) {
            // Calculate honor based on SINTA level or Scopus quartile
            BigDecimal honor = calculatePublicationHonor(publication, baseHonor);

            LecturerSalaryDetail detail = new LecturerSalaryDetail();
            detail.setComponentType(SalaryComponentType.PUBLICATION);
            detail.setDescription("Publikasi: " + publication.getPublicationTitle());

            // Add tier info to description
            if (publication.isScopusIndexed()) {
                detail.setDescription(detail.getDescription() + " (Scopus " + publication.getScopusQuartile() + ")");
            } else if (publication.isSintaIndexed()) {
                detail.setDescription(detail.getDescription() + " (SINTA " + publication.getSintaLevel() + ")");
            }

            detail.setQuantity(BigDecimal.ONE);
            detail.setRate(honor);
            detail.setAmount(honor);
            detail.setSourceReferenceId(publication.getId());
            detail.setSourceTable("publication_staging");

            salary.addDetail(detail);
            total = total.add(honor);
        }

        salary.setPublicationHonor(total);
    }

    private BigDecimal calculatePublicationHonor(PublicationStaging publication, BigDecimal baseHonor) {
        // Multipliers based on tier
        if (publication.isScopusIndexed()) {
            return switch (publication.getScopusQuartile()) {
                case "Q1" -> baseHonor.multiply(new BigDecimal("5.0"));
                case "Q2" -> baseHonor.multiply(new BigDecimal("4.0"));
                case "Q3" -> baseHonor.multiply(new BigDecimal("3.0"));
                case "Q4" -> baseHonor.multiply(new BigDecimal("2.0"));
                default -> baseHonor.multiply(new BigDecimal("1.5"));
            };
        } else if (publication.isSintaIndexed()) {
            int level = publication.getSintaLevelNumeric();
            return switch (level) {
                case 1, 2 -> baseHonor.multiply(new BigDecimal("3.0")); // SINTA 1-2
                case 3, 4 -> baseHonor.multiply(new BigDecimal("2.0")); // SINTA 3-4
                case 5, 6 -> baseHonor.multiply(new BigDecimal("1.5")); // SINTA 5-6
                default -> baseHonor;
            };
        }
        return baseHonor;
    }

    private void markStagingDataAsProcessed(Long lecturerId, String period, LecturerEmploymentStatus lecturerType) {
        // Mark teaching schedule/attendance as processed
        if (lecturerType == LecturerEmploymentStatus.DOSEN_TETAP) {
            List<TeachingScheduleStaging> schedules = teachingScheduleStagingRepository
                    .findUnprocessedByLecturerId(lecturerId);
            schedules.forEach(s -> s.setPayrollPeriodUsed(period));
            teachingScheduleStagingRepository.saveAll(schedules);
        } else {
            List<TeachingAttendanceStaging> attendances = teachingAttendanceStagingRepository
                    .findUnprocessedByLecturerId(lecturerId);
            attendances.forEach(a -> a.setPayrollPeriodUsed(period));
            teachingAttendanceStagingRepository.saveAll(attendances);
        }

        // Mark thesis guidance as processed
        List<ThesisGuidanceStaging> guidance = thesisGuidanceStagingRepository
                .findUnprocessedByLecturerId(lecturerId);
        guidance.forEach(g -> g.setPayrollPeriodUsed(period));
        thesisGuidanceStagingRepository.saveAll(guidance);

        // Mark thesis examination as processed
        List<ThesisExaminationStaging> examinations = thesisExaminationStagingRepository
                .findUnprocessedByLecturerId(lecturerId);
        examinations.forEach(e -> e.setPayrollPeriodUsed(period));
        thesisExaminationStagingRepository.saveAll(examinations);

        // Mark research as processed
        List<ResearchStaging> research = researchStagingRepository
                .findUnprocessedByLecturerId(lecturerId);
        research.forEach(r -> {
            r.setPayrollPeriodUsed(period);
            r.setIsProcessed(true);
        });
        researchStagingRepository.saveAll(research);

        // Mark publications as processed
        List<PublicationStaging> publications = publicationStagingRepository
                .findUnprocessedByLecturerId(lecturerId);
        publications.forEach(p -> {
            p.setPayrollPeriodUsed(period);
            p.setIsProcessed(true);
        });
        publicationStagingRepository.saveAll(publications);
    }

    @Transactional
    public void markAsPaid(Long salaryId) {
        LecturerSalary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("Salary not found"));

        if (salary.getStatus() != LecturerSalaryStatus.CALCULATED) {
            throw new IllegalArgumentException("Only calculated salaries can be marked as paid");
        }

        salary.markAsPaid();
        salaryRepository.save(salary);
    }

    @Transactional
    public void deleteSalary(Long salaryId) {
        LecturerSalary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("Salary not found"));

        if (salary.getStatus() == LecturerSalaryStatus.PAID) {
            throw new IllegalArgumentException("Cannot delete paid salary");
        }

        salary.setDeletedAt(LocalDateTime.now());
        salaryRepository.save(salary);

        // Reset staging data payroll period
        // This is a simplified approach - in production you might want more sophisticated tracking
    }
}
