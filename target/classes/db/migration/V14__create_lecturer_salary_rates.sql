-- V14: Create Master Tables for Lecturer Salary Rates
-- This migration creates tables to store configurable salary rates based on academic rank

-- Create Lecturer Salary Rates Table
-- Stores all configurable rates for lecturer salary calculation based on academic rank
CREATE TABLE lecturer_salary_rates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    academic_rank VARCHAR(50) NOT NULL UNIQUE COMMENT 'Academic rank: ASISTEN_AHLI, LEKTOR, LEKTOR_KEPALA, PROFESOR',

    -- Basic Salary & Allowances for Permanent Lecturers
    basic_salary DECIMAL(15, 2) DEFAULT 0 COMMENT 'Basic salary for permanent lecturers',
    functional_allowance DECIMAL(15, 2) DEFAULT 0 COMMENT 'Functional allowance based on academic rank',

    -- Teaching Rates for Contract Lecturers (per SKS)
    contract_sks_rate_offline DECIMAL(15, 2) DEFAULT 0 COMMENT 'Rate per SKS for contract lecturers (offline)',
    contract_sks_rate_online DECIMAL(15, 2) DEFAULT 0 COMMENT 'Rate per SKS for contract lecturers (online)',

    -- Overtime Teaching Rates for Permanent Lecturers (beyond obligation)
    overtime_sks_rate_offline DECIMAL(15, 2) DEFAULT 0 COMMENT 'Rate per additional SKS for permanent lecturers (offline)',
    overtime_sks_rate_online DECIMAL(15, 2) DEFAULT 0 COMMENT 'Rate per additional SKS for permanent lecturers (online)',

    -- Thesis Guidance & Examination Rates (per student/session)
    thesis_guidance_rate_offline DECIMAL(15, 2) DEFAULT 0 COMMENT 'Rate per student for thesis guidance (offline)',
    thesis_guidance_rate_online DECIMAL(15, 2) DEFAULT 0 COMMENT 'Rate per student for thesis guidance (online)',
    thesis_examination_rate_offline DECIMAL(15, 2) DEFAULT 0 COMMENT 'Rate per examination for thesis examiner (offline)',
    thesis_examination_rate_online DECIMAL(15, 2) DEFAULT 0 COMMENT 'Rate per examination for thesis examiner (online)',

    -- Research & Publication Honors
    research_honor DECIMAL(15, 2) DEFAULT 0 COMMENT 'Fixed honor per research',
    publication_honor DECIMAL(15, 2) DEFAULT 0 COMMENT 'Base honor per publication (can be overridden by tier)',

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,

    INDEX idx_academic_rank (academic_rank),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Master table for lecturer salary rates by academic rank';

-- Insert Default Data for Academic Ranks
INSERT INTO lecturer_salary_rates (
    academic_rank,
    basic_salary,
    functional_allowance,
    contract_sks_rate_offline,
    contract_sks_rate_online,
    overtime_sks_rate_offline,
    overtime_sks_rate_online,
    thesis_guidance_rate_offline,
    thesis_guidance_rate_online,
    thesis_examination_rate_offline,
    thesis_examination_rate_online,
    research_honor,
    publication_honor
) VALUES
(
    'ASISTEN_AHLI',
    5000000.00,   -- Basic salary
    500000.00,     -- Functional allowance
    150000.00,     -- Contract SKS offline
    100000.00,     -- Contract SKS online
    200000.00,     -- Overtime SKS offline
    150000.00,     -- Overtime SKS online
    500000.00,     -- Thesis guidance offline
    300000.00,     -- Thesis guidance online
    300000.00,     -- Thesis examination offline
    200000.00,     -- Thesis examination online
    2000000.00,    -- Research honor
    1000000.00     -- Publication honor
),
(
    'LEKTOR',
    7000000.00,
    750000.00,
    200000.00,
    150000.00,
    250000.00,
    200000.00,
    750000.00,
    500000.00,
    500000.00,
    350000.00,
    3000000.00,
    1500000.00
),
(
    'LEKTOR_KEPALA',
    9000000.00,
    1000000.00,
    250000.00,
    200000.00,
    300000.00,
    250000.00,
    1000000.00,
    750000.00,
    750000.00,
    500000.00,
    5000000.00,
    2500000.00
),
(
    'PROFESOR',
    12000000.00,
    1500000.00,
    300000.00,
    250000.00,
    400000.00,
    350000.00,
    1500000.00,
    1000000.00,
    1000000.00,
    750000.00,
    7500000.00,
    5000000.00
);
