-- =====================================================
-- HRIS Sederhana - Database Schema
-- Version: 1.0
-- Date: 16 Januari 2026
-- =====================================================

-- =====================================================
-- 1. TABLE: companies (Singleton - hanya 1 data)
-- =====================================================
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- === DATA PERUSAHAAN/INSTITUSI ===
    name VARCHAR(200) NOT NULL COMMENT 'Nama perusahaan/institusi',
    code VARCHAR(50) UNIQUE COMMENT 'Kode perusahaan (untuk reports)',
    type ENUM('COMPANY', 'UNIVERSITY', 'SCHOOL', 'OTHER') DEFAULT 'UNIVERSITY' COMMENT 'Tipe institusi',

    -- === ALAMAT & KONTAK ===
    address TEXT COMMENT 'Alamat lengkap perusahaan',
    city VARCHAR(100) COMMENT 'Kota',
    province VARCHAR(100) COMMENT 'Provinsi',
    postal_code VARCHAR(10) COMMENT 'Kode pos',
    phone VARCHAR(20) COMMENT 'Nomor telepon',
    email VARCHAR(100) COMMENT 'Email perusahaan',
    website VARCHAR(255) COMMENT 'Website',

    -- === DATA LEGAL ===
    npwp_company VARCHAR(25) UNIQUE COMMENT 'NPWP Perusahaan',
    siup_number VARCHAR(50) COMMENT 'Nomor SIUP',
    siup_expired_date DATE COMMENT 'Tanggal expired SIUP',
    establishment_date DATE COMMENT 'Tanggal pendirian perusahaan',

    -- === DATA BPJS PERUSAHAAN ===
    bpjs_ketenagakerjaan_no VARCHAR(30) COMMENT 'Nomor BPJS Ketenagakerjaan Perusahaan',
    bpjs_kesehatan_no VARCHAR(30) COMMENT 'Nomor BPJS Kesehatan Perusahaan',

    -- === DATA KEUANGAN ===
    tax_address TEXT COMMENT 'Alamat untuk pajak',
    bank_name VARCHAR(100) COMMENT 'Bank untuk payroll',
    bank_account_number VARCHAR(50) COMMENT 'Nomor rekening perusahaan',
    bank_account_name VARCHAR(100) COMMENT 'Nama pemilik rekening',

    -- === BRANDING ===
    logo_path VARCHAR(255) COMMENT 'Path logo perusahaan',
    stamp_path VARCHAR(255) COMMENT 'Path stempel/tanda tangan digital (untuk slip gaji)',

    -- === KONFIGURASI ===
    working_days VARCHAR(50) DEFAULT 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY' COMMENT 'Hari kerja',
    clock_in_start TIME DEFAULT '08:00:00' COMMENT 'Jam masuk kerja (mulai)',
    clock_in_end TIME DEFAULT '09:00:00' COMMENT 'Jam masuk kerja (akhir - batang terlambat)',
    clock_out_start TIME DEFAULT '17:00:00' COMMENT 'Jam pulang kerja (mulai)',
    clock_out_end TIME DEFAULT '18:00:00' COMMENT 'Jam pulang kerja (akhir)',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    INDEX idx_companies_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. TABLE: departments (created before employees)
-- =====================================================
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_prodi BOOLEAN DEFAULT FALSE COMMENT 'True jika ini adalah Program Studi',
    kode_prodi VARCHAR(20) COMMENT 'Kode program studi (jika is_prodi = true)',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    INDEX idx_departments_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. TABLE: positions (created before employees)
-- =====================================================
CREATE TABLE IF NOT EXISTS positions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_salary DECIMAL(15, 2) COMMENT 'Gaji pokok berdasarkan jabatan',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    INDEX idx_positions_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. TABLE: employees (created after departments & positions)
-- =====================================================
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- === DATA IDENTITAS (Per UU Ketenagakerjaan & BPJS) ===
    nik VARCHAR(20) UNIQUE NOT NULL COMMENT 'Nomor Induk Kependudukan',
    full_name VARCHAR(100) NOT NULL COMMENT 'Nama lengkap sesuai KTP',
    place_of_birth VARCHAR(50) COMMENT 'Tempat lahir',
    date_of_birth DATE NOT NULL COMMENT 'Tanggal lahir',
    gender ENUM('MALE', 'FEMALE') NOT NULL COMMENT 'Jenis kelamin',
    mothers_name VARCHAR(100) COMMENT 'Nama ibu kandung (wajib untuk BPJS)',

    -- === ALAMAT & KONTAK ===
    address TEXT COMMENT 'Alamat lengkap',
    phone VARCHAR(20) COMMENT 'Nomor telepon',
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'Hashed password',

    -- === DATA KEPEGAWAIAN (Per Pasal 185 UU 13/2003) ===
    employment_status ENUM('PERMANENT', 'CONTRACT', 'PROBATION', 'DAILY') DEFAULT 'PERMANENT' COMMENT 'Status kepegawaian',
    hire_date DATE NOT NULL COMMENT 'Tanggal mulai bekerja',
    work_location VARCHAR(100) COMMENT 'Lokasi kerja',

    -- === DATA BPJS (Wajib per Peraturan Pemerintah) ===
    bpjs_ketenagakerjaan_no VARCHAR(20) UNIQUE COMMENT 'Nomor BPJS Ketenagakerjaan',
    bpjs_kesehatan_no VARCHAR(20) UNIQUE COMMENT 'Nomor BPJS Kesehatan',
    npwp VARCHAR(20) UNIQUE COMMENT 'Nomor Pokok Wajib Pajak',

    -- === DATA GAJI (Untuk BPJS & Payroll) ===
    basic_salary DECIMAL(15, 2) COMMENT 'Gaji pokok (untuk BPJS & payroll)',

    -- === DATA ORGANISASI ===
    department_id BIGINT COMMENT 'Department/Unit kerja',
    position_id BIGINT COMMENT 'Jabatan/Position',

    -- === DATA TAMBAHAN ===
    kk_number VARCHAR(20) COMMENT 'Nomor Kartu Keluarga',
    photo_path VARCHAR(255) COMMENT 'Path foto profil',
    marital_status ENUM('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED') COMMENT 'Status pernikahan',
    spouse_name VARCHAR(100) COMMENT 'Nama pasangan (jika menikah)',
    number_of_dependents INT DEFAULT 0 COMMENT 'Jumlah tanggungan (untuk pajak)',

    -- === STATUS ===
    status ENUM('ACTIVE', 'INACTIVE', 'RESIGNED') DEFAULT 'ACTIVE' COMMENT 'Status employee',
    resignation_date DATE COMMENT 'Tanggal resign (jika status=RESIGNED)',
    resignation_reason TEXT COMMENT 'Alasan resign',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (position_id) REFERENCES positions(id),

    INDEX idx_employees_deleted_at (deleted_at),
    INDEX idx_employees_email (email),
    INDEX idx_employees_nik (nik),
    INDEX idx_employees_department (department_id),
    INDEX idx_employees_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. TABLE: employee_roles (junction table for many-to-many)
-- =====================================================
CREATE TABLE IF NOT EXISTS employee_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    role ENUM('ADMIN', 'HR', 'EMPLOYEE', 'DOSEN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang assign role',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus role',

    FOREIGN KEY (employee_id) REFERENCES employees(id),

    UNIQUE KEY unique_employee_role (employee_id, role, deleted_at),
    INDEX idx_employee_roles_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. TABLE: user_sessions
-- =====================================================
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    selected_role ENUM('ADMIN', 'HR', 'EMPLOYEE', 'DOSEN') NOT NULL,
    session_token VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    INDEX idx_user_sessions_token (session_token),
    INDEX idx_user_sessions_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. TABLE: lecturer_profiles
-- =====================================================
CREATE TABLE IF NOT EXISTS lecturer_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL UNIQUE,
    nidn VARCHAR(20) UNIQUE COMMENT 'Nomor Induk Dosen Nasional',
    last_education VARCHAR(100) COMMENT 'Pendidikan terakhir',
    expertise VARCHAR(255) COMMENT 'Bidang keahlian',
    lecturer_rank ENUM('ASISTEN_AHLI', 'LEKTOR', 'LEKTOR_KEPALA', 'PROFESOR') COMMENT 'Jenjang dosen',
    employment_status ENUM('DOSEN_TETAP', 'DOSEN_TIDAK_TETAP') DEFAULT 'DOSEN_TETAP' COMMENT 'Status kepegawaian dosen',
    work_status ENUM('ACTIVE', 'LEAVE', 'RETIRED') DEFAULT 'ACTIVE' COMMENT 'Status kerja dosen',
    homebase_prodi_id BIGINT COMMENT 'Department (prodi) homebase',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'User yang membuat data',
    updated_by BIGINT COMMENT 'User yang terakhir mengupdate',

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (homebase_prodi_id) REFERENCES departments(id),

    INDEX idx_lecturer_profiles_deleted_at (deleted_at),
    INDEX idx_lecturer_profiles_nidn (nidn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. TABLE: lecturer_salaries
-- =====================================================
CREATE TABLE IF NOT EXISTS lecturer_salaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lecturer_profile_id BIGINT NOT NULL,
    period VARCHAR(7) NOT NULL COMMENT 'Format: YYYY-MM',
    basic_salary DECIMAL(15, 2) DEFAULT 0 COMMENT 'Gaji pokok',
    certification DECIMAL(15, 2) DEFAULT 0 COMMENT 'Tunjangan sertifikasi',
    functional_allowance DECIMAL(15, 2) DEFAULT 0 COMMENT 'Tunjangan fungsional',
    teaching_honor DECIMAL(15, 2) DEFAULT 0 COMMENT 'Honor mengajar per SKS',
    other_allowances DECIMAL(15, 2) DEFAULT 0 COMMENT 'Tunjangan lain-lain',
    total_salary DECIMAL(15, 2) NOT NULL COMMENT 'Total gaji',
    status ENUM('DRAFT', 'PAID') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (lecturer_profile_id) REFERENCES lecturer_profiles(id),
    UNIQUE KEY unique_lecturer_period (lecturer_profile_id, period),
    INDEX idx_lecturer_salaries_period (period),
    INDEX idx_lecturer_salaries_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. TABLE: attendances
-- =====================================================
CREATE TABLE IF NOT EXISTS attendances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    clock_in TIME COMMENT 'Jam clock in',
    clock_out TIME COMMENT 'Jam clock out',
    status ENUM('PRESENT', 'LATE', 'LEAVE', 'SICK', 'ABSENT') DEFAULT 'PRESENT',
    notes TEXT,

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),

    UNIQUE KEY unique_employee_date (employee_id, date),
    INDEX idx_attendances_deleted_at (deleted_at),
    INDEX idx_attendances_date (date),
    INDEX idx_attendances_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 10. TABLE: leave_requests
-- =====================================================
CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    leave_type ENUM('ANNUAL', 'SICK', 'MATERNITY', 'MARRIAGE', 'SPECIAL', 'UNPAID') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by BIGINT COMMENT 'Employee yang approve',
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT COMMENT 'Alasan penolakan',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (approved_by) REFERENCES employees(id),

    INDEX idx_leave_requests_deleted_at (deleted_at),
    INDEX idx_leave_requests_employee (employee_id),
    INDEX idx_leave_requests_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 11. TABLE: leave_balances
-- =====================================================
CREATE TABLE IF NOT EXISTS leave_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    year INT NOT NULL,
    annual_total INT DEFAULT 12 COMMENT 'Total cuti tahunan',
    annual_used INT DEFAULT 0 COMMENT 'Cuti tahunan yang sudah dipakai',
    sick_total INT DEFAULT 14 COMMENT 'Total cuti sakit',
    sick_used INT DEFAULT 0 COMMENT 'Cuti sakit yang sudah dipakai',

    -- === AUDIT ===
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- === SOFT DELETE ===
    deleted_at TIMESTAMP NULL COMMENT 'Waktu soft delete (NULL = aktif)',
    deleted_by BIGINT COMMENT 'User yang menghapus data',

    FOREIGN KEY (employee_id) REFERENCES employees(id),

    UNIQUE KEY unique_employee_year (employee_id, year),
    INDEX idx_leave_balances_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 12. TABLE: payrolls
-- =====================================================
CREATE TABLE IF NOT EXISTS payrolls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    period VARCHAR(7) NOT NULL COMMENT 'Format: YYYY-MM',
    basic_salary DECIMAL(15, 2) NOT NULL COMMENT 'Gaji pokok',
    allowances DECIMAL(15, 2) DEFAULT 0 COMMENT 'Tunjangan',
    overtime DECIMAL(15, 2) DEFAULT 0 COMMENT 'Lembur',
    deductions DECIMAL(15, 2) DEFAULT 0 COMMENT 'Potongan (BPJS, PPh21, dll)',
    total_salary DECIMAL(15, 2) NOT NULL COMMENT 'Total gaji bersih',
    status ENUM('DRAFT', 'PAID') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    UNIQUE KEY unique_employee_period (employee_id, period),
    INDEX idx_payrolls_period (period),
    INDEX idx_payrolls_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 13. TABLE: user_activity_logs
-- =====================================================
CREATE TABLE IF NOT EXISTS user_activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT COMMENT 'User yang melakukan aktivitas (null untuk system)',
    activity_type ENUM('LOGIN', 'LOGOUT', 'ROLE_SELECTION', 'CREATE', 'READ', 'UPDATE', 'DELETE', 'RESTORE',
                       'CLOCK_IN', 'CLOCK_OUT', 'SUBMIT_LEAVE', 'APPROVE_LEAVE', 'REJECT_LEAVE',
                       'GENERATE_PAYROLL', 'VIEW_SENSITIVE_DATA') NOT NULL,
    module_name VARCHAR(100) COMMENT 'Nama modul (employee, department, dll)',
    entity_type VARCHAR(100) COMMENT 'Tipe entity',
    entity_id BIGINT COMMENT 'ID entity',
    description VARCHAR(500) COMMENT 'Deskripsi aktivitas',
    activity_details JSON COMMENT 'Detail aktivitas dalam format JSON',
    status ENUM('SUCCESS', 'FAILED') DEFAULT 'SUCCESS',
    error_message TEXT COMMENT 'Error message jika failed',
    ip_address VARCHAR(45) COMMENT 'IP address user',
    user_agent VARCHAR(500) COMMENT 'User agent (browser/device)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    INDEX idx_activity_logs_employee (employee_id),
    INDEX idx_activity_logs_type (activity_type),
    INDEX idx_activity_logs_module (module_name),
    INDEX idx_activity_logs_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
