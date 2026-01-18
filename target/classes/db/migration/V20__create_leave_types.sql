-- Create leave_types table for global leave type settings
CREATE TABLE IF NOT EXISTS leave_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    year INT NOT NULL COMMENT 'Tahun berlaku untuk setting ini',
    code VARCHAR(10) NOT NULL UNIQUE COMMENT 'Kode cuti: AL, SK, MT, MK, SP',
    name VARCHAR(100) NOT NULL COMMENT 'Nama jenis cuti',
    leave_type ENUM('QUOTA', 'NO_QUOTA') NOT NULL DEFAULT 'QUOTA' COMMENT 'QUOTA = berkuota, NO_QUOTA = tidak berkuota',
    annual_quota INT DEFAULT 0 COMMENT 'Jatah cuti per tahun (hanya jika QUOTA)',

    -- Carry Forward Settings
    allow_carry_forward BOOLEAN DEFAULT false COMMENT 'Izinkan carry forward cuti tahun lalu?',
    max_carry_forward_days INT DEFAULT 6 COMMENT 'Maksimal hari yang bisa di-carry forward',
    carry_forward_expiry_month INT DEFAULT 3 COMMENT 'Bulan carry forward expired (1-12, default 3=Maret)',
    carry_forward_expiry_day INT DEFAULT 31 COMMENT 'Tanggal carry forward expired (1-31)',

    -- Employee Criteria
    min_years_of_service INT DEFAULT 0 COMMENT 'Masa kerja minimum dalam tahun (0 = semua karyawan)',
    gender_restriction ENUM('ALL', 'MALE', 'FEMALE') DEFAULT 'ALL' COMMENT 'Batasan gender (jika ada)',
    is_paid BOOLEAN DEFAULT true COMMENT 'Cuti dibayar atau tidak',
    require_proof BOOLEAN DEFAULT false COMMENT 'Perlu bukti (surat dokter, dll)',
    proof_description VARCHAR(255) COMMENT 'Deskripsi bukti yang diperlukan',

    description TEXT COMMENT 'Deskripsi lengkap jenis cuti',
    is_active BOOLEAN DEFAULT true COMMENT 'Status aktif/non-aktif',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,

    INDEX idx_year_code (year, code),
    INDEX idx_year_active (year, is_active),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabel setting jenis cuti global';

-- Insert default leave types for year 2026
INSERT INTO leave_types (year, code, name, leave_type, annual_quota, allow_carry_forward, max_carry_forward_days, carry_forward_expiry_month, carry_forward_expiry_day, min_years_of_service, gender_restriction, is_paid, require_proof, description, is_active) VALUES
-- Cuti Tahunan (Annual Leave)
(2026, 'AL', 'Cuti Tahunan', 'QUOTA', 12, true, 6, 3, 31, 1, 'ALL', true, false, 'Cuti tahunan sesuai UU No. 13 Tahun 2003. Sisa cuti tahun sebelumnya bisa di-carry forward maksimal 6 hari dan expired 31 Maret.', true),

-- Cuti Sakit (Sick Leave)
(2026, 'SK', 'Cuti Sakit', 'NO_QUOTA', NULL, false, NULL, NULL, NULL, 0, 'ALL', true, true, 'Cuti sakit dengan surat dokter. Tidak terbatas kuota namun perlu bukti medis.', true),

-- Cuti Melahirkan (Maternity Leave)
(2026, 'MT', 'Cuti Melahirkan', 'QUOTA', 90, false, NULL, NULL, NULL, 1, 'FEMALE', true, true, 'Cuti melahirkan 90 hari sesuai UU No. 13 Tahun 2003. Hanya untuk karyawan wanita dengan masa kerja minimal 1 tahun.', true),

-- Cuti Menikah (Marriage Leave)
(2026, 'MK', 'Cuti Menikah', 'QUOTA', 3, false, NULL, NULL, NULL, 0, 'ALL', true, true, 'Cuti menikah 3 hari untuk karyawan yang menikah. Perlu bukti surat nikah.', true),

-- Cuti Khusus (Special Leave)
(2026, 'SP', 'Cuti Khusus', 'NO_QUOTA', NULL, false, NULL, NULL, NULL, 0, 'ALL', false, true, 'Cuti khusus untuk keperluan mendesak/tidak terduga. Tidak dibayar dan perlu approval khusus.', true),

-- Cuti Ibadah (Pilgrimage Leave)
(2026, 'IB', 'Cuti Ibadah', 'QUOTA', 30, false, NULL, NULL, NULL, 2, 'ALL', true, true, 'Cuti ibadah (haji/umroh) 30 hari. Untuk karyawan dengan masa kerja minimal 2 tahun. Perlu bukti pendaftaran.', true);
