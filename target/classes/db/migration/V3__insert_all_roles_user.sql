-- =====================================================
-- HRIS Sederhana - Test User with All Roles
-- Version: 3.0
-- Date: 16 Januari 2026
-- =====================================================

-- =====================================================
-- Insert Test User with ALL Roles (ADMIN, HR, EMPLOYEE, DOSEN)
-- Password: test123 (hashed with BCrypt)
-- Email: allroles@hris.local
-- =====================================================
INSERT INTO employees (
    nik, full_name, email, password, place_of_birth, date_of_birth, gender,
    employment_status, hire_date, work_location,
    department_id, position_id, status,
    created_at, updated_at
) VALUES (
    'TEST001',
    'Test User All Roles',
    'allroles@hris.local',
    '$2a$10$5RMJxPyfwoUo6.9tG8/L0eqP1GU3H3UvqKKlCWU9q8RK8GpLY2p1u',
    'Jakarta',
    '1995-05-15',
    'MALE',
    'PERMANENT',
    CURDATE(),
    'Kantor Pusat',
    1, -- Biro Administrasi
    1, -- Staff Administrasi
    'ACTIVE',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE id=id;

-- Assign ALL roles to this test user
INSERT INTO employee_roles (employee_id, role, created_at, created_by)
SELECT e.id, 'ADMIN', NOW(), e.id FROM employees e WHERE e.email = 'allroles@hris.local';

INSERT INTO employee_roles (employee_id, role, created_at, created_by)
SELECT e.id, 'HR', NOW(), e.id FROM employees e WHERE e.email = 'allroles@hris.local';

INSERT INTO employee_roles (employee_id, role, created_at, created_by)
SELECT e.id, 'EMPLOYEE', NOW(), e.id FROM employees e WHERE e.email = 'allroles@hris.local';

INSERT INTO employee_roles (employee_id, role, created_at, created_by)
SELECT e.id, 'DOSEN', NOW(), e.id FROM employees e WHERE e.email = 'allroles@hris.local';
