-- =====================================================
-- HRIS Sederhana - Default Company Data
-- Version: 11
-- Description: Insert default company data (singleton)
-- =====================================================

-- Insert default company data
-- This is a singleton record - only ONE company should exist
INSERT INTO companies (
    name,
    code,
    type,
    address,
    city,
    province,
    postal_code,
    phone,
    email,
    website,
    npwp_company,
    siup_number,
    establishment_date,
    bpjs_ketenagakerjaan_no,
    bpjs_kesehatan_no,
    bank_name,
    bank_account_number,
    bank_account_name,
    working_days,
    clock_in_start,
    clock_in_end,
    clock_out_start,
    clock_out_end,
    created_at,
    updated_at
) VALUES (
    'Universitas Contoh',
    'UNIV001',
    'UNIVERSITY',
    'Jl. Pendidikan No. 123',
    'Jakarta',
    'DKI Jakarta',
    '12345',
    '(021) 1234567',
    'info@univcontoh.ac.id',
    'https://www.univcontoh.ac.id',
    '01.234.567.8-123.456',
    '1234567890',
    '1990-01-01',
    '12345678',
    '87654321',
    'Bank Central Asia',
    '1234567890',
    'Universitas Contoh',
    'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',
    '08:00:00',
    '09:00:00',
    '17:00:00',
    '18:00:00',
    NOW(),
    NOW()
);
