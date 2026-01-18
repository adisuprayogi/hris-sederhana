-- Create leave_balances table
-- Menyimpan saldo cuti karyawan per tahun
CREATE TABLE leave_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    year INT NOT NULL,
    annual_quota INT NOT NULL DEFAULT 12,
    balance DOUBLE NOT NULL DEFAULT 0,
    used DOUBLE NOT NULL DEFAULT 0,
    carried_forward DOUBLE DEFAULT 0,
    carried_forward_expiry_date DATE NULL,
    expired_balance DOUBLE DEFAULT 0,
    total_deduction DOUBLE DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    deleted_by BIGINT NULL,
    INDEX idx_leave_balance_employee (employee_id),
    INDEX idx_leave_balance_year (year),
    INDEX idx_leave_balance_employee_year (employee_id, year),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add unique constraint for employee_id + year
ALTER TABLE leave_balances ADD CONSTRAINT uk_employee_year UNIQUE (employee_id, year);
