-- =====================================================
-- V16: Create Shift System
-- 3-Layer Shift Structure:
-- 1. Working Hours (Master Jam Kerja)
-- 2. Shift Packages (Kombinasi Working Hours per Hari)
-- 3. Shift Patterns (Shift Package + Permissions)
-- 4. Employee Shift Settings (Assignment dengan date range)
-- 5. Employee Shift Schedules (Override per tanggal)
-- =====================================================

-- =====================================================
-- 1. working_hours - Master Jam Kerja
-- =====================================================
CREATE TABLE working_hours (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description VARCHAR(255),

    start_time TIME NULL,
    end_time TIME NULL,
    is_overnight BOOLEAN DEFAULT FALSE,

    break_duration_minutes INT DEFAULT 60,
    required_work_hours DECIMAL(4,2) DEFAULT 8.00,

    display_order INT DEFAULT 0,
    color VARCHAR(20) DEFAULT '#3B82F6',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT
);

-- Insert default working hours
INSERT INTO working_hours (name, code, description, start_time, end_time, is_overnight, display_order, color) VALUES
('OFF', 'WH_OFF', 'Hari Libur', NULL, NULL, FALSE, 0, '#9CA3AF'),
('Pagi 1', 'WH_PAGI1', '08:00 - 16:00', '08:00:00', '16:00:00', FALSE, 1, '#10B981'),
('Pagi 2', 'WH_PAGI2', '07:00 - 15:00', '07:00:00', '15:00:00', FALSE, 2, '#34D399'),
('Siang', 'WH_SIANG', '14:00 - 22:00', '14:00:00', '22:00:00', FALSE, 3, '#F59E0B'),
('Malam', 'WH_MALAM', '22:00 - 06:00', '22:00:00', '06:00:00', TRUE, 4, '#6366F1'),
('Flexible', 'WH_FLEX', 'Flexible Start Time', NULL, NULL, FALSE, 5, '#8B5CF6');

-- =====================================================
-- 2. shift_packages - Paket Shift (Schedule per Hari)
-- =====================================================
CREATE TABLE shift_packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,

    sunday_working_hours_id BIGINT,
    monday_working_hours_id BIGINT NOT NULL,
    tuesday_working_hours_id BIGINT NOT NULL,
    wednesday_working_hours_id BIGINT NOT NULL,
    thursday_working_hours_id BIGINT NOT NULL,
    friday_working_hours_id BIGINT NOT NULL,
    saturday_working_hours_id BIGINT,

    display_order INT DEFAULT 0,
    color VARCHAR(20) DEFAULT '#3B82F6',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,

    FOREIGN KEY (sunday_working_hours_id) REFERENCES working_hours(id),
    FOREIGN KEY (monday_working_hours_id) REFERENCES working_hours(id),
    FOREIGN KEY (tuesday_working_hours_id) REFERENCES working_hours(id),
    FOREIGN KEY (wednesday_working_hours_id) REFERENCES working_hours(id),
    FOREIGN KEY (thursday_working_hours_id) REFERENCES working_hours(id),
    FOREIGN KEY (friday_working_hours_id) REFERENCES working_hours(id),
    FOREIGN KEY (saturday_working_hours_id) REFERENCES working_hours(id)
);

-- Insert default shift package: Shift Standard (Mon-Fri)
INSERT INTO shift_packages (name, code, description,
    sunday_working_hours_id, monday_working_hours_id, tuesday_working_hours_id,
    wednesday_working_hours_id, thursday_working_hours_id, friday_working_hours_id,
    saturday_working_hours_id, display_order, color)
SELECT
    'Shift Standard 5 Hari',
    'SP_STANDARD',
    'Senin-Jumat kerja, Sabtu-Minggu libur',
    (SELECT id FROM working_hours WHERE code = 'WH_OFF'),
    (SELECT id FROM working_hours WHERE code = 'WH_PAGI1'),
    (SELECT id FROM working_hours WHERE code = 'WH_PAGI1'),
    (SELECT id FROM working_hours WHERE code = 'WH_PAGI1'),
    (SELECT id FROM working_hours WHERE code = 'WH_PAGI1'),
    (SELECT id FROM working_hours WHERE code = 'WH_PAGI1'),
    (SELECT id FROM working_hours WHERE code = 'WH_OFF'),
    1,
    '#3B82F6';

-- =====================================================
-- 3. shift_patterns - Pattern (Shift Package + Permissions)
-- =====================================================
CREATE TABLE shift_patterns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,

    shift_package_id BIGINT NOT NULL,

    shift_type VARCHAR(20) DEFAULT 'FIXED',

    flexible_start_window_start TIME NULL,
    flexible_start_window_end TIME NULL,
    flexible_required_hours DECIMAL(4,2) NULL,

    is_overtime_allowed BOOLEAN DEFAULT FALSE,
    is_wfh_allowed BOOLEAN DEFAULT FALSE,
    is_attendance_mandatory BOOLEAN DEFAULT TRUE,

    late_tolerance_minutes INT DEFAULT 0,
    early_leave_tolerance_minutes INT DEFAULT 0,

    late_deduction_per_minute DECIMAL(15,2) DEFAULT 0,
    late_deduction_max_amount DECIMAL(15,2) DEFAULT 0,
    underwork_deduction_per_minute DECIMAL(15,2) DEFAULT 0,
    underwork_deduction_max_amount DECIMAL(15,2) DEFAULT 0,

    color VARCHAR(20) DEFAULT '#3B82F6',
    display_order INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,

    FOREIGN KEY (shift_package_id) REFERENCES shift_packages(id)
);

-- Insert default shift pattern: Shift Standard
INSERT INTO shift_patterns (name, code, shift_package_id, shift_type,
    is_overtime_allowed, is_wfh_allowed, is_attendance_mandatory,
    late_tolerance_minutes, color, display_order)
SELECT
    'Shift Standard',
    'ST001',
    (SELECT id FROM shift_packages WHERE code = 'SP_STANDARD'),
    'FIXED',
    TRUE,
    FALSE,
    TRUE,
    15,
    '#3B82F6',
    1;

-- =====================================================
-- 4. employee_shift_settings - Assignment dengan Date Range
-- =====================================================
CREATE TABLE employee_shift_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,

    shift_pattern_id BIGINT NOT NULL,

    effective_from DATE NOT NULL,
    effective_to DATE NULL,

    reason VARCHAR(255),
    notes TEXT,

    created_by BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (shift_pattern_id) REFERENCES shift_patterns(id),
    INDEX idx_employee_date (employee_id, effective_from, effective_to),
    UNIQUE KEY uk_employee_effective_date (employee_id, effective_from)
);

-- =====================================================
-- 5. employee_shift_schedules - Override per Tanggal
-- =====================================================
CREATE TABLE employee_shift_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    schedule_date DATE NOT NULL,

    working_hours_id BIGINT NULL,

    override_is_wfh BOOLEAN NULL,
    override_is_overtime_allowed BOOLEAN NULL,
    override_attendance_mandatory BOOLEAN NULL,

    notes TEXT,

    created_by BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (working_hours_id) REFERENCES working_hours(id),
    UNIQUE KEY uk_employee_schedule_date (employee_id, schedule_date)
);

-- =====================================================
-- 6. Update positions table - Add default_shift_pattern_id
-- =====================================================
ALTER TABLE positions ADD COLUMN default_shift_pattern_id BIGINT NULL;
ALTER TABLE positions ADD FOREIGN KEY (default_shift_pattern_id) REFERENCES shift_patterns(id);

-- =====================================================
-- 7. Update departments table - Add default_shift_pattern_id
-- =====================================================
ALTER TABLE departments ADD COLUMN default_shift_pattern_id BIGINT NULL;
ALTER TABLE departments ADD FOREIGN KEY (default_shift_pattern_id) REFERENCES shift_patterns(id);

-- =====================================================
-- 8. Update companies table - Add default_shift_pattern_id
-- =====================================================
ALTER TABLE companies ADD COLUMN default_shift_pattern_id BIGINT NULL;
ALTER TABLE companies ADD FOREIGN KEY (default_shift_pattern_id) REFERENCES shift_patterns(id);

-- Update company with default shift pattern
UPDATE companies SET default_shift_pattern_id = (SELECT id FROM shift_patterns WHERE code = 'ST001') WHERE id = 1;
