-- =====================================================
-- Sprint 7: Attendance Management Migration
-- =====================================================
-- This migration adds:
-- 1. Complete 2-level approval workflow for leave_requests
-- 2. wfh_requests table (2-level approval)
-- 3. overtime_requests table (2-level approval)
-- 4. attendance_records table
-- 5. Office location to companies table
-- =====================================================

-- =====================================================
-- 1. CLEANUP and UPDATE leave_requests for 2-level approval
-- =====================================================

-- Drop the old foreign key (if exists) using prepared statement
SET @exist_fk = (SELECT COUNT(*) FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
    AND table_name = 'leave_requests'
    AND constraint_name = 'leave_requests_ibfk_2'
    AND constraint_type = 'FOREIGN KEY');
SET @sql_drop_fk = IF(@exist_fk > 0,
    'ALTER TABLE leave_requests DROP FOREIGN KEY leave_requests_ibfk_2',
    'SELECT "FK does not exist, skipping"');
PREPARE stmt FROM @sql_drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop old columns one by one (MySQL doesn't support DROP COLUMN IF EXISTS)
SET @exist_col1 = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'approved_by');
SET @sql_drop1 = IF(@exist_col1 > 0, 'ALTER TABLE leave_requests DROP COLUMN approved_by', 'SELECT "Column approved_by does not exist"');
PREPARE stmt FROM @sql_drop1;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_col2 = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'approved_at');
SET @sql_drop2 = IF(@exist_col2 > 0, 'ALTER TABLE leave_requests DROP COLUMN approved_at', 'SELECT "Column approved_at does not exist"');
PREPARE stmt FROM @sql_drop2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_col3 = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'rejection_reason');
SET @sql_drop3 = IF(@exist_col3 > 0, 'ALTER TABLE leave_requests DROP COLUMN rejection_reason', 'SELECT "Column rejection_reason does not exist"');
PREPARE stmt FROM @sql_drop3;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop old index if exists
SET @exist_idx = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = 'leave_requests'
    AND index_name = 'approved_by');
SET @sql_drop_idx = IF(@exist_idx > 0,
    'ALTER TABLE leave_requests DROP INDEX approved_by',
    'SELECT "Index does not exist, skipping"');
PREPARE stmt FROM @sql_drop_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop old fk_lr_current_approver if exists
SET @exist_fk2 = (SELECT COUNT(*) FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
    AND table_name = 'leave_requests'
    AND constraint_name = 'fk_lr_current_approver'
    AND constraint_type = 'FOREIGN KEY');
SET @sql_drop_fk2 = IF(@exist_fk2 > 0,
    'ALTER TABLE leave_requests DROP FOREIGN KEY fk_lr_current_approver',
    'SELECT "FK fk_lr_current_approver does not exist, skipping"');
PREPARE stmt FROM @sql_drop_fk2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop old index idx_lr_current_approver if exists
SET @exist_idx2 = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = 'leave_requests'
    AND index_name = 'idx_lr_current_approver');
SET @sql_drop_idx2 = IF(@exist_idx2 > 0,
    'ALTER TABLE leave_requests DROP INDEX idx_lr_current_approver',
    'SELECT "Index idx_lr_current_approver does not exist, skipping"');
PREPARE stmt FROM @sql_drop_idx2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop current_approver_id column (after FK and index are dropped)
SET @exist_col4 = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'current_approver_id');
SET @sql_drop4 = IF(@exist_col4 > 0, 'ALTER TABLE leave_requests DROP COLUMN current_approver_id', 'SELECT "Column current_approver_id does not exist"');
PREPARE stmt FROM @sql_drop4;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add new columns for 2-level approval (if not exists)
SET @exist_sup = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'supervisor_id');
SET @sql_add_sup = IF(@exist_sup = 0,
    'ALTER TABLE leave_requests ADD COLUMN supervisor_id BIGINT NULL COMMENT "Supervisor (atasan langsung) yang approve/reject" AFTER employee_id',
    'SELECT "Column supervisor_id already exists"');
PREPARE stmt FROM @sql_add_sup;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_sup_at = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'supervisor_approved_at');
SET @sql_add_sup_at = IF(@exist_sup_at = 0,
    'ALTER TABLE leave_requests ADD COLUMN supervisor_approved_at TIMESTAMP NULL COMMENT "Waktu supervisor approve/reject"',
    'SELECT "Column supervisor_approved_at already exists"');
PREPARE stmt FROM @sql_add_sup_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_sup_note = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'supervisor_approval_note');
SET @sql_add_sup_note = IF(@exist_sup_note = 0,
    'ALTER TABLE leave_requests ADD COLUMN supervisor_approval_note TEXT COMMENT "Catatan approval/rejection dari supervisor"',
    'SELECT "Column supervisor_approval_note already exists"');
PREPARE stmt FROM @sql_add_sup_note;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_hr = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'hr_id');
SET @sql_add_hr = IF(@exist_hr = 0,
    'ALTER TABLE leave_requests ADD COLUMN hr_id BIGINT NULL COMMENT "HR/Admin yang approve/reject"',
    'SELECT "Column hr_id already exists"');
PREPARE stmt FROM @sql_add_hr;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_hr_at = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'hr_approved_at');
SET @sql_add_hr_at = IF(@exist_hr_at = 0,
    'ALTER TABLE leave_requests ADD COLUMN hr_approved_at TIMESTAMP NULL COMMENT "Waktu HR approve/reject"',
    'SELECT "Column hr_approved_at already exists"');
PREPARE stmt FROM @sql_add_hr_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_hr_note = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND column_name = 'hr_approval_note');
SET @sql_add_hr_note = IF(@exist_hr_note = 0,
    'ALTER TABLE leave_requests ADD COLUMN hr_approval_note TEXT COMMENT "Catatan approval/rejection dari HR"',
    'SELECT "Column hr_approval_note already exists"');
PREPARE stmt FROM @sql_add_hr_note;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update status ENUM to support 2-level approval
ALTER TABLE leave_requests
    MODIFY COLUMN status ENUM('PENDING_SUPERVISOR', 'PENDING_HR', 'APPROVED', 'REJECTED_BY_SUPERVISOR', 'REJECTED_BY_HR')
    DEFAULT 'PENDING_SUPERVISOR'
    COMMENT 'Status approval: PENDING_SUPERVISOR=menunggu atasan, PENDING_HR=menunggu HR, APPROVED=disetujui, REJECTED_BY_SUPERVISOR=ditolak atasan, REJECTED_BY_HR=ditolak HR';

-- Add indexes for 2-level approval queries (if not exists)
SET @exist_sup_idx = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND index_name = 'idx_leave_requests_supervisor');
SET @sql_sup_idx = IF(@exist_sup_idx = 0, 'CREATE INDEX idx_leave_requests_supervisor ON leave_requests(supervisor_id)', 'SELECT "Index already exists"');
PREPARE stmt FROM @sql_sup_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_hr_idx = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND index_name = 'idx_leave_requests_hr');
SET @sql_hr_idx = IF(@exist_hr_idx = 0, 'CREATE INDEX idx_leave_requests_hr ON leave_requests(hr_id)', 'SELECT "Index already exists"');
PREPARE stmt FROM @sql_hr_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign keys for supervisor and hr (if not exists)
SET @exist_fk_sup = (SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND constraint_name = 'fk_leave_requests_supervisor' AND constraint_type = 'FOREIGN KEY');
SET @sql_fk_sup = IF(@exist_fk_sup = 0, 'ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_supervisor FOREIGN KEY (supervisor_id) REFERENCES employees(id) ON DELETE SET NULL', 'SELECT "FK already exists"');
PREPARE stmt FROM @sql_fk_sup;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_fk_hr = (SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'leave_requests' AND constraint_name = 'fk_leave_requests_hr' AND constraint_type = 'FOREIGN KEY');
SET @sql_fk_hr = IF(@exist_fk_hr = 0, 'ALTER TABLE leave_requests ADD CONSTRAINT fk_leave_requests_hr FOREIGN KEY (hr_id) REFERENCES employees(id) ON DELETE SET NULL', 'SELECT "FK already exists"');
PREPARE stmt FROM @sql_fk_hr;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- 2. CREATE wfh_requests table
-- =====================================================

CREATE TABLE IF NOT EXISTS wfh_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT 'Employee yang mengajukan WFH',
    request_date DATE NOT NULL COMMENT 'Tanggal WFH yang diminta',
    reason TEXT COMMENT 'Alasan WFH',
    status ENUM('PENDING_SUPERVISOR', 'PENDING_HR', 'APPROVED', 'REJECTED_BY_SUPERVISOR', 'REJECTED_BY_HR')
        DEFAULT 'PENDING_SUPERVISOR'
        COMMENT 'Status approval',
    supervisor_id BIGINT NULL COMMENT 'Supervisor yang approve/reject',
    supervisor_approved_at TIMESTAMP NULL COMMENT 'Waktu supervisor approve/reject',
    supervisor_approval_note TEXT COMMENT 'Catatan supervisor',
    hr_id BIGINT NULL COMMENT 'HR/Admin yang approve/reject',
    hr_approved_at TIMESTAMP NULL COMMENT 'Waktu HR approve/reject',
    hr_approval_note TEXT COMMENT 'Catatan HR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang mengupdate data',
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (supervisor_id) REFERENCES employees(id) ON DELETE SET NULL,
    FOREIGN KEY (hr_id) REFERENCES employees(id) ON DELETE SET NULL,
    INDEX idx_wfh_requests_deleted_at (deleted_at),
    INDEX idx_wfh_requests_employee (employee_id),
    INDEX idx_wfh_requests_status (status),
    INDEX idx_wfh_requests_date (request_date),
    INDEX idx_wfh_requests_supervisor (supervisor_id),
    INDEX idx_wfh_requests_hr (hr_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='WFH (Work From Home) requests dengan 2-level approval';

-- =====================================================
-- 3. CREATE overtime_requests table
-- =====================================================

CREATE TABLE IF NOT EXISTS overtime_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT 'Employee yang mengajukan lembur',
    request_date DATE NOT NULL COMMENT 'Tanggal lembur yang diminta',
    estimated_hours DECIMAL(5,2) NOT NULL COMMENT 'Estimasi jam lembur',
    reason TEXT NOT NULL COMMENT 'Alasan lembur',
    actual_duration_minutes INT NULL COMMENT 'Durasi aktual lembur dalam menit',
    actual_work_description TEXT NULL COMMENT 'Deskripsi pekerjaan lembur',
    status ENUM('PENDING_SUPERVISOR', 'PENDING_HR', 'APPROVED', 'REJECTED_BY_SUPERVISOR', 'REJECTED_BY_HR')
        DEFAULT 'PENDING_SUPERVISOR'
        COMMENT 'Status approval',
    supervisor_id BIGINT NULL COMMENT 'Supervisor yang approve/reject',
    supervisor_approved_at TIMESTAMP NULL COMMENT 'Waktu supervisor approve/reject',
    supervisor_approval_note TEXT COMMENT 'Catatan supervisor',
    hr_id BIGINT NULL COMMENT 'HR/Admin yang approve/reject',
    hr_approved_at TIMESTAMP NULL COMMENT 'Waktu HR approve/reject',
    hr_approval_note TEXT COMMENT 'Catatan HR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang mengupdate data',
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (supervisor_id) REFERENCES employees(id) ON DELETE SET NULL,
    FOREIGN KEY (hr_id) REFERENCES employees(id) ON DELETE SET NULL,
    INDEX idx_overtime_requests_deleted_at (deleted_at),
    INDEX idx_overtime_requests_employee (employee_id),
    INDEX idx_overtime_requests_status (status),
    INDEX idx_overtime_requests_date (request_date),
    INDEX idx_overtime_requests_supervisor (supervisor_id),
    INDEX idx_overtime_requests_hr (hr_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Overtime (Lembur) requests dengan 2-level approval';

-- =====================================================
-- 4. CREATE attendance_records table
-- =====================================================

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT 'Employee',
    attendance_date DATE NOT NULL COMMENT 'Tanggal attendance',
    clock_in_time TIME NULL COMMENT 'Waktu clock in',
    clock_in_latitude DECIMAL(10, 8) NULL COMMENT 'Latitude clock in',
    clock_in_longitude DECIMAL(11, 8) NULL COMMENT 'Longitude clock in',
    clock_in_device_info VARCHAR(255) NULL COMMENT 'Info device clock in',
    clock_in_photo_path VARCHAR(500) NULL COMMENT 'Path foto clock in',
    clock_out_time TIME NULL COMMENT 'Waktu clock out',
    clock_out_latitude DECIMAL(10, 8) NULL COMMENT 'Latitude clock out',
    clock_out_longitude DECIMAL(11, 8) NULL COMMENT 'Longitude clock out',
    clock_out_device_info VARCHAR(255) NULL COMMENT 'Info device clock out',
    clock_out_photo_path VARCHAR(500) NULL COMMENT 'Path foto clock out',
    working_hours_id BIGINT NULL COMMENT 'Snapshot reference ke working_hours',
    shift_pattern_id BIGINT NULL COMMENT 'Snapshot reference ke shift_pattern',
    is_late BOOLEAN DEFAULT FALSE COMMENT 'Apakah terlambat',
    late_duration_minutes INT DEFAULT 0 COMMENT 'Durasi terlambat dalam menit',
    late_deduction_amount DECIMAL(15, 2) DEFAULT 0.00 COMMENT 'Potongan terlambat',
    is_early_leave BOOLEAN DEFAULT FALSE COMMENT 'Apakah pulang awal',
    early_leave_duration_minutes INT DEFAULT 0 COMMENT 'Durasi pulang awal dalam menit',
    early_leave_deduction_amount DECIMAL(15, 2) DEFAULT 0.00 COMMENT 'Potongan pulang awal',
    is_overtime BOOLEAN DEFAULT FALSE COMMENT 'Apakah lembur',
    overtime_duration_minutes INT DEFAULT 0 COMMENT 'Durasi lembur dalam menit',
    actual_work_minutes INT DEFAULT 0 COMMENT 'Durasi kerja aktual dalam menit',
    required_work_minutes INT DEFAULT 0 COMMENT 'Durasi kerja yang dibutuhkan dalam menit',
    underwork_minutes INT DEFAULT 0 COMMENT 'Deficit durasi kerja dalam menit',
    underwork_deduction_amount DECIMAL(15, 2) DEFAULT 0.00 COMMENT 'Potongan underwork',
    status ENUM('PRESENT', 'LATE', 'EARLY_LEAVE', 'ABSENT', 'WFH')
        DEFAULT 'PRESENT'
        COMMENT 'Status attendance',
    is_wfh BOOLEAN DEFAULT FALSE COMMENT 'Apakah WFH (Work From Home)',
    notes TEXT NULL COMMENT 'Catatan tambahan',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang mengupdate data',
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (working_hours_id) REFERENCES working_hours(id) ON DELETE SET NULL,
    FOREIGN KEY (shift_pattern_id) REFERENCES shift_patterns(id) ON DELETE SET NULL,
    INDEX idx_attendance_employee_date (employee_id, attendance_date),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_attendance_status (status),
    INDEX idx_attendance_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Catatan attendance harian employee dengan clock in/out, location, late, overtime';

-- =====================================================
-- 5. UPDATE companies table with office location
-- =====================================================

SET @exist_col_lat = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'office_latitude');
SET @sql_add_lat = IF(@exist_col_lat = 0,
    'ALTER TABLE companies ADD COLUMN office_latitude DECIMAL(10, 8) NULL COMMENT ''Latitude lokasi kantor'' AFTER logo_path',
    'SELECT "Column office_latitude already exists"');
PREPARE stmt FROM @sql_add_lat;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_col_lon = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'office_longitude');
SET @sql_add_lon = IF(@exist_col_lon = 0,
    'ALTER TABLE companies ADD COLUMN office_longitude DECIMAL(11, 8) NULL COMMENT ''Longitude lokasi kantor'' AFTER office_latitude',
    'SELECT "Column office_longitude already exists"');
PREPARE stmt FROM @sql_add_lon;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_col_radius = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'companies' AND column_name = 'attendance_location_radius');
SET @sql_add_radius = IF(@exist_col_radius = 0,
    'ALTER TABLE companies ADD COLUMN attendance_location_radius INT DEFAULT 100 COMMENT ''Radius lokasi attendance (meter, max 150)'' AFTER office_longitude',
    'SELECT "Column attendance_location_radius already exists"');
PREPARE stmt FROM @sql_add_radius;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
