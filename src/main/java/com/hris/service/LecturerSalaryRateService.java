package com.hris.service;

import com.hris.model.LecturerSalaryRate;
import com.hris.model.enums.LecturerRank;
import com.hris.repository.LecturerSalaryRateRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class LecturerSalaryRateService {

    @Autowired
    private LecturerSalaryRateRepository repository;

    public List<LecturerSalaryRate> getAllRates() {
        return repository.findAllOrderByAcademicRank();
    }

    public List<LecturerSalaryRate> getAllActiveRates() {
        return repository.findAllByDeletedAtIsNull();
    }

    public Optional<LecturerSalaryRate> getRateById(Long id) {
        return repository.findById(id);
    }

    public Optional<LecturerSalaryRate> getRateByLecturerRank(LecturerRank academicRank) {
        return repository.findByAcademicRankAndDeletedAtIsNull(academicRank);
    }

    @Transactional
    public LecturerSalaryRate createRate(LecturerSalaryRate rate) {
        validateRate(rate);
        return repository.save(rate);
    }

    @Transactional
    public LecturerSalaryRate updateRate(Long id, LecturerSalaryRate rate) {
        LecturerSalaryRate existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("LecturerSalaryRate not found with id: " + id));

        // Check if academic rank is being changed to one that already exists
        if (!existing.getAcademicRank().equals(rate.getAcademicRank())) {
            if (repository.existsByAcademicRankAndDeletedAtIsNull(rate.getAcademicRank())) {
                throw new IllegalArgumentException("Rate already exists for academic rank: " + rate.getAcademicRank());
            }
        }

        validateRate(rate);

        existing.setAcademicRank(rate.getAcademicRank());
        existing.setBasicSalary(rate.getBasicSalary());
        existing.setFunctionalAllowance(rate.getFunctionalAllowance());
        existing.setContractSksRateOffline(rate.getContractSksRateOffline());
        existing.setContractSksRateOnline(rate.getContractSksRateOnline());
        existing.setOvertimeSksRateOffline(rate.getOvertimeSksRateOffline());
        existing.setOvertimeSksRateOnline(rate.getOvertimeSksRateOnline());
        existing.setThesisGuidanceRateOffline(rate.getThesisGuidanceRateOffline());
        existing.setThesisGuidanceRateOnline(rate.getThesisGuidanceRateOnline());
        existing.setThesisExaminationRateOffline(rate.getThesisExaminationRateOffline());
        existing.setThesisExaminationRateOnline(rate.getThesisExaminationRateOnline());
        existing.setResearchHonor(rate.getResearchHonor());
        existing.setPublicationHonor(rate.getPublicationHonor());

        return repository.save(existing);
    }

    @Transactional
    public void deleteRate(Long id) {
        LecturerSalaryRate rate = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("LecturerSalaryRate not found with id: " + id));

        rate.setDeletedAt(java.time.LocalDateTime.now());
        repository.save(rate);
    }

    public BigDecimal getContractSksRate(LecturerRank academicRank, boolean isOnline) {
        Optional<LecturerSalaryRate> rateOpt = getRateByLecturerRank(academicRank);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No rate configured for academic rank: " + academicRank);
        }

        LecturerSalaryRate rate = rateOpt.get();
        return isOnline ? rate.getContractSksRateOnline() : rate.getContractSksRateOffline();
    }

    public BigDecimal getOvertimeSksRate(LecturerRank academicRank, boolean isOnline) {
        Optional<LecturerSalaryRate> rateOpt = getRateByLecturerRank(academicRank);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No rate configured for academic rank: " + academicRank);
        }

        LecturerSalaryRate rate = rateOpt.get();
        return isOnline ? rate.getOvertimeSksRateOnline() : rate.getOvertimeSksRateOffline();
    }

    public BigDecimal getThesisGuidanceRate(LecturerRank academicRank, boolean isOnline) {
        Optional<LecturerSalaryRate> rateOpt = getRateByLecturerRank(academicRank);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No rate configured for academic rank: " + academicRank);
        }

        LecturerSalaryRate rate = rateOpt.get();
        return isOnline ? rate.getThesisGuidanceRateOnline() : rate.getThesisGuidanceRateOffline();
    }

    public BigDecimal getThesisExaminationRate(LecturerRank academicRank, boolean isOnline) {
        Optional<LecturerSalaryRate> rateOpt = getRateByLecturerRank(academicRank);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No rate configured for academic rank: " + academicRank);
        }

        LecturerSalaryRate rate = rateOpt.get();
        return isOnline ? rate.getThesisExaminationRateOnline() : rate.getThesisExaminationRateOffline();
    }

    public BigDecimal getResearchHonor(LecturerRank academicRank) {
        Optional<LecturerSalaryRate> rateOpt = getRateByLecturerRank(academicRank);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No rate configured for academic rank: " + academicRank);
        }
        return rateOpt.get().getResearchHonor();
    }

    public BigDecimal getPublicationHonor(LecturerRank academicRank) {
        Optional<LecturerSalaryRate> rateOpt = getRateByLecturerRank(academicRank);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No rate configured for academic rank: " + academicRank);
        }
        return rateOpt.get().getPublicationHonor();
    }

    public BigDecimal getBasicSalary(LecturerRank academicRank) {
        Optional<LecturerSalaryRate> rateOpt = getRateByLecturerRank(academicRank);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No rate configured for academic rank: " + academicRank);
        }
        return rateOpt.get().getBasicSalary();
    }

    public BigDecimal getFunctionalAllowance(LecturerRank academicRank) {
        Optional<LecturerSalaryRate> rateOpt = getRateByLecturerRank(academicRank);
        if (rateOpt.isEmpty()) {
            throw new IllegalArgumentException("No rate configured for academic rank: " + academicRank);
        }
        return rateOpt.get().getFunctionalAllowance();
    }

    private void validateRate(LecturerSalaryRate rate) {
        if (rate.getAcademicRank() == null) {
            throw new IllegalArgumentException("Academic rank is required");
        }

        // Check uniqueness
        if (rate.getId() == null) {
            // Creating new rate
            if (repository.existsByAcademicRankAndDeletedAtIsNull(rate.getAcademicRank())) {
                throw new IllegalArgumentException("Rate already exists for academic rank: " + rate.getAcademicRank());
            }
        }

        // Validate that rates are not negative
        if (rate.getBasicSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Basic salary cannot be negative");
        }
        if (rate.getFunctionalAllowance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Functional allowance cannot be negative");
        }
        if (rate.getContractSksRateOffline().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Contract SKS rate offline cannot be negative");
        }
        if (rate.getContractSksRateOnline().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Contract SKS rate online cannot be negative");
        }
        if (rate.getOvertimeSksRateOffline().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Overtime SKS rate offline cannot be negative");
        }
        if (rate.getOvertimeSksRateOnline().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Overtime SKS rate online cannot be negative");
        }
    }

    public boolean hasAllRanksConfigured() {
        long count = repository.findAllByDeletedAtIsNull().size();
        return count == LecturerRank.values().length;
    }
}
