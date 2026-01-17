package com.hris.service;

import com.hris.model.Position;
import com.hris.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Position Service
 * Handles business logic for position management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Get all active positions
     */
    public List<Position> getAllPositions() {
        return positionRepository.findAllActive();
    }

    /**
     * Search positions with filters and pagination
     */
    public Page<Position> searchPositions(String search, Integer level, Pageable pageable) {
        return positionRepository.searchPositions(search, level, pageable);
    }

    /**
     * Get position by ID
     * @Transactional ensures lazy relationships can be accessed within the same session
     */
    @Transactional(readOnly = true)
    public Position getPositionById(Long id) {
        return positionRepository.findById(id)
                .filter(pos -> pos.getDeletedAt() == null)
                .orElse(null);
    }

    /**
     * Create new position
     */
    @Transactional
    public Position createPosition(Position position) {
        log.info("Creating new position: {}", position.getName());

        // Validate: name must be unique
        if (positionRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(position.getName())) {
            throw new IllegalArgumentException("Nama position sudah ada: " + position.getName());
        }

        // Validate: level must be between 1 and 6
        if (position.getLevel() == null || position.getLevel() < 1 || position.getLevel() > 6) {
            throw new IllegalArgumentException("Level position harus antara 1 dan 6");
        }

        Position saved = positionRepository.save(position);
        log.info("Position created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update existing position
     */
    @Transactional
    public Position updatePosition(Long id, Position position) {
        log.info("Updating position ID: {}", id);

        Position existing = getPositionById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Position tidak ditemukan dengan ID: " + id);
        }

        // Validate: name uniqueness (exclude current position)
        if (!existing.getName().equalsIgnoreCase(position.getName()) &&
            positionRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(position.getName())) {
            throw new IllegalArgumentException("Nama position sudah ada: " + position.getName());
        }

        // Validate: level must be between 1 and 6
        if (position.getLevel() == null || position.getLevel() < 1 || position.getLevel() > 6) {
            throw new IllegalArgumentException("Level position harus antara 1 dan 6");
        }

        // Update fields
        existing.setName(position.getName());
        existing.setDescription(position.getDescription());
        existing.setLevel(position.getLevel());
        existing.setBaseSalary(position.getBaseSalary());

        Position saved = positionRepository.save(existing);
        log.info("Position updated successfully: {}", saved.getId());
        return saved;
    }

    /**
     * Soft delete position
     */
    @Transactional
    public void deletePosition(Long id) {
        log.info("Deleting position ID: {}", id);

        Position position = getPositionById(id);
        if (position == null) {
            throw new IllegalArgumentException("Position tidak ditemukan dengan ID: " + id);
        }

        position.setDeletedAt(java.time.LocalDateTime.now());
        positionRepository.save(position);

        log.info("Position deleted successfully: {}", id);
    }

    // =====================================================
    // QUERY METHODS
    // =====================================================

    /**
     * Get positions by level
     */
    public List<Position> getPositionsByLevel(Integer level) {
        return positionRepository.findByLevelAndDeletedAtIsNull(level);
    }

    /**
     * Get positions by minimum level
     */
    public List<Position> getPositionsByMinLevel(Integer minLevel) {
        return positionRepository.findByLevelGreaterThanEqualAndDeletedAtIsNull(minLevel);
    }

    /**
     * Count positions by level
     */
    public long countPositionsByLevel(Integer level) {
        return positionRepository.countByLevelAndDeletedAtIsNull(level);
    }
}
