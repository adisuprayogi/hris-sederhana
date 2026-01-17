-- Add separate payroll cutoff and payment dates for each employee/lecturer type
-- This allows different payroll schedules for Employees, Permanent Lecturers, and Contract Lecturers

-- Add employee payroll dates
ALTER TABLE companies ADD COLUMN employee_payroll_cutoff_date INT DEFAULT 25 AFTER employee_payroll_period;
ALTER TABLE companies ADD COLUMN employee_payroll_payment_date INT DEFAULT 1 AFTER employee_payroll_cutoff_date;

-- Add permanent lecturer payroll dates
ALTER TABLE companies ADD COLUMN permanent_lecturer_payroll_cutoff_date INT DEFAULT 25 AFTER permanent_lecturer_payroll_period;
ALTER TABLE companies ADD COLUMN permanent_lecturer_payroll_payment_date INT DEFAULT 1 AFTER permanent_lecturer_payroll_cutoff_date;

-- Add contract lecturer payroll dates
ALTER TABLE companies ADD COLUMN contract_lecturer_payroll_cutoff_date INT DEFAULT 25 AFTER contract_lecturer_payroll_period;
ALTER TABLE companies ADD COLUMN contract_lecturer_payroll_payment_date INT DEFAULT 1 AFTER contract_lecturer_payroll_cutoff_date;

-- Update existing company records with default values
UPDATE companies
SET
    employee_payroll_cutoff_date = 25,
    employee_payroll_payment_date = 1,
    permanent_lecturer_payroll_cutoff_date = 25,
    permanent_lecturer_payroll_payment_date = 1,
    contract_lecturer_payroll_cutoff_date = 25,
    contract_lecturer_payroll_payment_date = 1
WHERE id IS NOT NULL;
