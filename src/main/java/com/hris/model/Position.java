package com.hris.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Position Entity
 * Represents a job position/title with level hierarchy
 */
@Entity
@Table(name = "positions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Position extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_salary", precision = 15, scale = 2)
    private BigDecimal baseSalary;

    // =====================================================
    // APPROVAL STRUCTURE FIELDS
    // =====================================================

    /**
     * Position level in the organizational hierarchy
     * 1 = Staff
     * 2 = Senior Staff
     * 3 = Supervisor / Team Lead
     * 4 = Manager
     * 5 = Wakil Rektor / Dekan / Director
     * 6 = Rektor / President
     */
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Check if this position is at or above the specified level
     *
     * @param targetLevel The level to compare against
     * @return true if this position's level is >= targetLevel
     */
    public boolean isAtLeast(int targetLevel) {
        return this.level != null && this.level >= targetLevel;
    }

    /**
     * Check if this position is at or below the specified level
     *
     * @param targetLevel The level to compare against
     * @return true if this position's level is <= targetLevel
     */
    public boolean isAtMost(int targetLevel) {
        return this.level != null && this.level <= targetLevel;
    }

    /**
     * Get the level name as a human-readable string
     *
     * @return The level name (e.g., "Staff", "Manager", "Rektor")
     */
    public String getLevelName() {
        if (this.level == null) {
            return "Unknown";
        }

        return switch (this.level) {
            case 1 -> "Staff";
            case 2 -> "Senior Staff";
            case 3 -> "Supervisor";
            case 4 -> "Manager";
            case 5 -> "Wakil Rektor/Dekan";
            case 6 -> "Rektor";
            default -> "Level " + this.level;
        };
    }

    /**
     * Position level enum for type-safe level handling
     */
    public enum Level {
        STAFF(1, "Staff"),
        SENIOR_STAFF(2, "Senior Staff"),
        SUPERVISOR(3, "Supervisor"),
        MANAGER(4, "Manager"),
        WAREK_DEKAN(5, "Wakil Rektor/Dekan"),
        REKTOR(6, "Rektor");

        private final int value;
        private final String name;

        Level(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public static Level fromValue(int value) {
            for (Level level : values()) {
                if (level.value == value) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Invalid level value: " + value);
        }
    }
}
