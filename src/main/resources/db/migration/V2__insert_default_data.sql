-- =====================================================
-- HRIS Sederhana - Default Data
-- Version: 2.0
-- Date: 16 Januari 2026
-- =====================================================

-- =====================================================
-- Insert Default Company
-- =====================================================
INSERT INTO companies (
    name, code, type, address, city, province, postal_code, phone, email, website,
    working_days, clock_in_start, clock_in_end, clock_out_start, clock_out_end,
    created_at, updated_at
) VALUES (
    'Universitas Contoh Indonesia',
    'UCI',
    'UNIVERSITY',
    'Jl. Pendidikan No. 123, Jakarta Selatan',
    'Jakarta Selatan',
    'DKI Jakarta',
    '12345',
    '(021) 1234567',
    'info@universitascontoh.ac.id',
    'https://www.universitascontoh.ac.id',
    'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',
    '08:00:00',
    '09:00:00',
    '17:00:00',
    '18:00:00',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE id=id;

-- =====================================================
-- Insert Default Departments
-- =====================================================
INSERT INTO departments (name, description, is_prodi, kode_prodi, created_at, updated_at) VALUES
('Biro Administrasi', 'Biro Administrasi Umum', FALSE, NULL, NOW(), NOW()),
('Fakultas Teknik', 'Fakultas Teknik', FALSE, NULL, NOW(), NOW()),
('Teknik Informatika', 'Program Studi Teknik Informatika', TRUE, 'TI-001', NOW(), NOW()),
('Teknik Sipil', 'Program Studi Teknik Sipil', TRUE, 'TS-001', NOW(), NOW()),
('Fakultas Ekonomi', 'Fakultas Ekonomi', FALSE, NULL, NOW(), NOW()),
('Manajemen', 'Program Studi Manajemen', TRUE, 'M-001', NOW(), NOW()),
('Akuntansi', 'Program Studi Akuntansi', TRUE, 'A-001', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- =====================================================
-- Insert Default Positions
-- =====================================================
INSERT INTO positions (name, description, base_salary, created_at, updated_at) VALUES
('Staff Administrasi', 'Staf Administrasi', 5000000, NOW(), NOW()),
('HR Staff', 'Staf HR', 5500000, NOW(), NOW()),
('HR Manager', 'Manager HR', 10000000, NOW(), NOW()),
('Asisten Ahli', 'Jenjang Dosen Asisten Ahli', 8000000, NOW(), NOW()),
('Lektor', 'Jenjang Dosen Lektor', 10000000, NOW(), NOW()),
('Lektor Kepala', 'Jenjang Dosen Lektor Kepala', 12000000, NOW(), NOW()),
('Profesor', 'Jenjang Dosen Profesor', 15000000, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- =====================================================
-- Insert Default Admin User
-- Password: admin123 (hashed with BCrypt)
-- =====================================================
INSERT INTO employees (
    nik, full_name, email, password, place_of_birth, date_of_birth, gender,
    employment_status, hire_date, work_location,
    department_id, position_id, status,
    created_at, updated_at
) VALUES (
    'ADMIN001',
    'Administrator',
    'admin@hris.local',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
    'Jakarta',
    '1990-01-01',
    'MALE',
    'PERMANENT',
    CURDATE(),
    'Kantor Pusat',
    1, -- Biro Administrasi
    2, -- HR Staff
    'ACTIVE',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE id=id;

-- Assign ADMIN and HR role to the admin user
INSERT INTO employee_roles (employee_id, role, created_at, created_by)
SELECT e.id, 'ADMIN', NOW(), e.id FROM employees e WHERE e.email = 'admin@hris.local';

INSERT INTO employee_roles (employee_id, role, created_at, created_by)
SELECT e.id, 'HR', NOW(), e.id FROM employees e WHERE e.email = 'admin@hris.local';
