-- Add payroll period configuration to companies table
-- This migration adds payroll period settings for different employee/lecturer types

-- Add employee payroll period column
ALTER TABLE companies ADD COLUMN employee_payroll_period VARCHAR(20) DEFAULT 'MONTHLY' AFTER clock_out_end;

-- Add permanent lecturer payroll period column
ALTER TABLE companies ADD COLUMN permanent_lecturer_payroll_period VARCHAR(20) DEFAULT 'MONTHLY' AFTER employee_payroll_period;

-- Add contract lecturer payroll period column
ALTER TABLE companies ADD COLUMN contract_lecturer_payroll_period VARCHAR(20) DEFAULT 'HOURLY' AFTER permanent_lecturer_payroll_period;

-- Add payroll cutoff date column (day of month when payroll calculation is done)
ALTER TABLE companies ADD COLUMN payroll_cutoff_date INT DEFAULT 25 AFTER contract_lecturer_payroll_period;

-- Add payroll payment date column (day of month when salary is paid)
ALTER TABLE companies ADD COLUMN payroll_payment_date INT DEFAULT 1 AFTER payroll_cutoff_date;

-- Update existing company record with default values
UPDATE companies
SET
    employee_payroll_period = 'MONTHLY',
    permanent_lecturer_payroll_period = 'MONTHLY',
    contract_lecturer_payroll_period = 'HOURLY',
    payroll_cutoff_date = 25,
    payroll_payment_date = 1
WHERE id IS NOT NULL;
