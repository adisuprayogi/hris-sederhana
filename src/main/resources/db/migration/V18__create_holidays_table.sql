-- Create holidays table
-- Menyimpan data hari libur (nasional, perusahaan, cuti bersama)
CREATE TABLE holidays (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    date DATE NOT NULL UNIQUE,
    year INT NOT NULL,
    holiday_type VARCHAR(20) NOT NULL COMMENT 'NATIONAL, COMPANY, COLLECTIVE_LEAVE',
    is_active BOOLEAN DEFAULT TRUE,
    description TEXT,
    repeat_annually BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    deleted_by BIGINT NULL,
    INDEX idx_holiday_date (date),
    INDEX idx_holiday_type (holiday_type),
    INDEX idx_holiday_year (year),
    INDEX idx_holiday_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample Indonesian national holidays for 2026
INSERT INTO holidays (name, date, year, holiday_type, is_active, description, repeat_annually) VALUES
('Tahun Baru Masehi', '2026-01-01', 2026, 'NATIONAL', TRUE, 'Peringatan tahun baru Masehi', TRUE),
('Tahun Baru Imlek', '2026-02-17', 2026, 'NATIONAL', TRUE, 'Tahun baru Imlek 2577', FALSE),
('Isra Miraj', '2026-02-25', 2026, 'NATIONAL', TRUE, 'Isra Miraj Nabi Muhammad SAW', FALSE),
('Hari Raya Nyepi', '2026-03-16', 2026, 'NATIONAL', TRUE, 'Tahun Baru Saka 1948', FALSE),
('Wafat Isa Al Masih', '2026-04-03', 2026, 'NATIONAL', TRUE, 'Wafat Isa Al Masih', FALSE),
('Hari Raya Idul Fitri', '2026-03-31', 2026, 'NATIONAL', TRUE, 'Hari Raya Idul Fitri 1447 H', FALSE),
('Hari Raya Idul Fitri', '2026-04-01', 2026, 'NATIONAL', TRUE, 'Hari Raya Idul Fitri 1447 H (Hari ke-2)', FALSE),
('Hari Raya Idul Adha', '2026-06-07', 2026, 'NATIONAL', TRUE, 'Hari Raya Idul Adha 1447 H', FALSE),
('Kenaikan Isa Al Masih', '2026-05-14', 2026, 'NATIONAL', TRUE, 'Kenaikan Isa Al Masih', FALSE),
('Hari Kemerdekaan RI', '2026-08-17', 2026, 'NATIONAL', TRUE, 'Hari Kemerdekaan Republik Indonesia ke-81', TRUE),
('Maulid Nabi Muhammad SAW', '2026-10-19', 2026, 'NATIONAL', TRUE, 'Maulid Nabi Muhammad SAW', FALSE),
('Hari Raya Natal', '2026-12-25', 2026, 'NATIONAL', TRUE, 'Hari Raya Natal', TRUE),
('Cuti Bersama', '2026-04-02', 2026, 'COLLECTIVE_LEAVE', TRUE, 'Cuti bersama Idul Fitri', FALSE),
('Cuti Bersama', '2026-08-18', 2026, 'COLLECTIVE_LEAVE', TRUE, 'Cuti bersama Kemerdekaan', FALSE);
