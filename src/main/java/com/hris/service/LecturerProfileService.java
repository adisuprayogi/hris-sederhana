package com.hris.service;

import com.hris.model.LecturerProfile;
import com.hris.model.enums.LecturerEmploymentStatus;
import com.hris.model.enums.LecturerRank;
import com.hris.model.enums.LecturerWorkStatus;
import com.hris.repository.LecturerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LecturerProfileService {

    @Autowired
    private LecturerProfileRepository repository;

    public List<LecturerProfile> getAllActiveProfiles() {
        return repository.findAllActive();
    }

    public List<LecturerProfile> getAllProfiles() {
        return repository.findAll();
    }

    public LecturerProfile getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public LecturerProfile getByIdWithEmployee(Long id) {
        return repository.findByIdWithEmployee(id).orElse(null);
    }

    public Optional<LecturerProfile> findByNidn(String nidn) {
        return repository.findByNidnAndDeletedAtIsNull(nidn);
    }

    public Optional<LecturerProfile> findByEmployeeId(Long employeeId) {
        return repository.findByEmployeeIdAndDeletedAtIsNull(employeeId);
    }

    public List<LecturerProfile> findByEmploymentStatus(LecturerEmploymentStatus status) {
        return repository.findByEmploymentStatusAndDeletedAtIsNull(status);
    }

    public List<LecturerProfile> findByWorkStatus(LecturerWorkStatus status) {
        return repository.findByWorkStatusAndDeletedAtIsNull(status);
    }

    public List<LecturerProfile> findByLecturerRank(LecturerRank rank) {
        return repository.findByLecturerRankAndDeletedAtIsNull(rank);
    }

    public List<LecturerProfile> searchLecturers(String search, LecturerRank rank,
                                                  LecturerEmploymentStatus empStatus,
                                                  LecturerWorkStatus workStatus,
                                                  Long prodiId) {
        return repository.searchLecturers(search, rank, empStatus, workStatus, prodiId);
    }

    public LecturerProfile save(LecturerProfile profile) {
        return repository.save(profile);
    }

    public void delete(LecturerProfile profile) {
        profile.setDeletedAt(java.time.LocalDateTime.now());
        repository.save(profile);
    }

    public long countActive() {
        return repository.countByDeletedAtIsNull();
    }

    public long countByEmploymentStatus(LecturerEmploymentStatus status) {
        return repository.countByEmploymentStatusAndDeletedAtIsNull(status);
    }

    public long countByWorkStatus(LecturerWorkStatus status) {
        return repository.countByWorkStatusAndDeletedAtIsNull(status);
    }

    public boolean existsByNidn(String nidn) {
        return repository.existsByNidnAndDeletedAtIsNull(nidn);
    }

    public boolean existsByNidnAndIdNot(String nidn, Long id) {
        return repository.existsByNidnAndDeletedAtIsNullAndIdNot(nidn, id);
    }
}
