-- V15: Create Staging Tables for External System Integration
-- These tables store data from external systems (Siakad, Research System, etc.)
-- before being processed into lecturer salary calculation

-- =====================================================
-- TEACHING SCHEDULE STAGING (From Siakad)
-- For Permanent Lecturers - Used to calculate teaching obligation & overtime
-- =====================================================
CREATE TABLE teaching_schedule_staging (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lecturer_id BIGINT NOT NULL COMMENT 'FK to lecturer_profiles or employees',
    academic_year VARCHAR(10) NOT NULL COMMENT 'e.g., 2024/2025',
    semester VARCHAR(20) NOT NULL COMMENT 'GANJIL, GENAP',
    course_code VARCHAR(50) NOT NULL,
    course_name VARCHAR(255),
    schedule_day VARCHAR(20) COMMENT 'SENIN, SELASA, etc.',
    schedule_time VARCHAR(50) COMMENT 'e.g., 08:00-10:00',
    sks DECIMAL(3, 1) NOT NULL COMMENT 'Credit hours',
    teaching_mode VARCHAR(20) DEFAULT 'OFFLINE' COMMENT 'ONLINE, OFFLINE',

    -- Processing flag
    payroll_period_used VARCHAR(10) COMMENT 'YYYY-MM when used for payroll, NULL if unused',

    -- Audit fields
    sync_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When data was synced from Siakad',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    INDEX idx_lecturer_id (lecturer_id),
    INDEX idx_period (academic_year, semester),
    INDEX idx_payroll_period (payroll_period_used),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Staging table for teaching schedule from Siakad';

-- =====================================================
-- TEACHING ATTENDANCE STAGING (From Siakad)
-- For Contract Lecturers - Used to calculate actual SKS taught
-- =====================================================
CREATE TABLE teaching_attendance_staging (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lecturer_id BIGINT NOT NULL COMMENT 'FK to lecturer_profiles or employees',
    attendance_date DATE NOT NULL,
    course_code VARCHAR(50) NOT NULL,
    course_name VARCHAR(255),
    sks DECIMAL(3, 1) NOT NULL COMMENT 'Credit hours attended',
    teaching_mode VARCHAR(20) DEFAULT 'OFFLINE' COMMENT 'ONLINE, OFFLINE',
    attendance_status VARCHAR(20) DEFAULT 'HADIR' COMMENT 'HADIR, TIDAK_HADIR',

    -- Processing flag
    payroll_period_used VARCHAR(10) COMMENT 'YYYY-MM when used for payroll, NULL if unused',

    -- Audit fields
    sync_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When data was synced from Siakad',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    INDEX idx_lecturer_id (lecturer_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_payroll_period (payroll_period_used),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Staging table for teaching attendance from Siakad';

-- =====================================================
-- THESIS GUIDANCE STAGING (From Siakad)
-- For thesis/supervision honor calculation
-- =====================================================
CREATE TABLE thesis_guidance_staging (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lecturer_id BIGINT NOT NULL COMMENT 'FK to lecturer_profiles or employees',
    student_id VARCHAR(50),
    student_name VARCHAR(255),
    thesis_type VARCHAR(50) COMMENT 'SKRIPSI_S1, TESIS_S2, DISERTASI_S3',
    guidance_mode VARCHAR(20) DEFAULT 'OFFLINE' COMMENT 'ONLINE, OFFLINE',
    guidance_session INT COMMENT 'Which session number (1, 2, 3, etc.)',

    -- Processing flag
    payroll_period_used VARCHAR(10) COMMENT 'YYYY-MM when used for payroll, NULL if unused',

    -- Audit fields
    sync_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When data was synced from Siakad',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    INDEX idx_lecturer_id (lecturer_id),
    INDEX idx_student_id (student_id),
    INDEX idx_payroll_period (payroll_period_used),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Staging table for thesis guidance from Siakad';

-- =====================================================
-- THESIS EXAMINATION STAGING (From Siakad)
-- For thesis examination honor calculation
-- =====================================================
CREATE TABLE thesis_examination_staging (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lecturer_id BIGINT NOT NULL COMMENT 'FK to lecturer_profiles or employees',
    thesis_id VARCHAR(50),
    student_id VARCHAR(50),
    student_name VARCHAR(255),
    thesis_type VARCHAR(50) COMMENT 'SKRIPSI_S1, TESIS_S2, DISERTASI_S3',
    examination_role VARCHAR(50) COMMENT 'PEMBIMBING, PENGUJI_1, PENGUJI_2, KETUA_PENGUJI',
    examination_mode VARCHAR(20) DEFAULT 'OFFLINE' COMMENT 'ONLINE, OFFLINE',
    examination_date DATE,

    -- Processing flag
    payroll_period_used VARCHAR(10) COMMENT 'YYYY-MM when used for payroll, NULL if unused',

    -- Audit fields
    sync_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When data was synced from Siakad',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    INDEX idx_lecturer_id (lecturer_id),
    INDEX idx_thesis_id (thesis_id),
    INDEX idx_payroll_period (payroll_period_used),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Staging table for thesis examination from Siakad';

-- =====================================================
-- RESEARCH STAGING (From Research System)
-- For research honor calculation
-- =====================================================
CREATE TABLE research_staging (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lecturer_id BIGINT NOT NULL COMMENT 'FK to lecturer_profiles or employees',
    research_title VARCHAR(500),
    research_type VARCHAR(100) COMMENT 'e.g., PENELITIAN_DASAR, PENELITIAN_TERAPAN',
    research_tier VARCHAR(50) COMMENT 'e.g., INTERNASIONAL, NASIONAL, LEMBAGA',
    research_duration_months INT COMMENT 'Duration in months',

    -- Processing flags
    is_processed BOOLEAN DEFAULT FALSE COMMENT 'TRUE if already included in payroll',
    payroll_period_used VARCHAR(10) COMMENT 'YYYY-MM when used for payroll',

    -- Audit fields
    report_date DATE COMMENT 'When research was reported',
    sync_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When data was synced from Research System',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    INDEX idx_lecturer_id (lecturer_id),
    INDEX idx_is_processed (is_processed),
    INDEX idx_payroll_period (payroll_period_used),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Staging table for research data';

-- =====================================================
-- PUBLICATION STAGING (From Publication System)
-- For publication honor calculation with SINTA & Scopus classification
-- =====================================================
CREATE TABLE publication_staging (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lecturer_id BIGINT NOT NULL COMMENT 'FK to lecturer_profiles or employees',
    publication_title VARCHAR(500),
    publication_type VARCHAR(50) COMMENT 'ARTICLE, PROCEEDING, BOOK_CHAPTER, BOOK',

    -- SINTA Classification (National)
    sinta_level VARCHAR(20) COMMENT 'SINTA_1, SINTA_2, SINTA_3, SINTA_4, SINTA_5, SINTA_6, NONE',

    -- Scopus Classification (International)
    is_scopus_indexed BOOLEAN DEFAULT FALSE,
    scopus_quartile VARCHAR(10) COMMENT 'Q1, Q2, Q3, Q4, NONE if not indexed',

    -- Publication details
    journal_name VARCHAR(255),
    publication_date DATE,
    doi VARCHAR(255) COMMENT 'Digital Object Identifier',
    authors TEXT COMMENT 'List of authors',

    -- Processing flags
    is_processed BOOLEAN DEFAULT FALSE COMMENT 'TRUE if already included in payroll',
    payroll_period_used VARCHAR(10) COMMENT 'YYYY-MM when used for payroll',

    -- Audit fields
    report_date DATE COMMENT 'When publication was reported',
    sync_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When data was synced from Publication System',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    INDEX idx_lecturer_id (lecturer_id),
    INDEX idx_sinta_level (sinta_level),
    INDEX idx_scopus_quartile (scopus_quartile),
    INDEX idx_is_processed (is_processed),
    INDEX idx_payroll_period (payroll_period_used),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Staging table for publication data with SINTA/Scopus classification';
