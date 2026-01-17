-- =====================================================
-- Employee History Tables
-- For tracking employee job changes and salary history
-- =====================================================

-- Employee Job History Table
-- Tracks department/position changes and promotions
CREATE TABLE IF NOT EXISTS employee_job_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,

    -- Job information
    department_id BIGINT,
    position_id BIGINT,

    -- Change details
    change_type VARCHAR(50) NOT NULL COMMENT 'HIRE, PROMOTION, TRANSFER, DEMOTION, RESIGNATION',
    change_reason TEXT,

    -- Effective dates
    effective_date DATE NOT NULL COMMENT 'When this change took effect',
    end_date DATE COMMENT 'When this job ended (NULL if current)',

    -- Status
    is_current BOOLEAN DEFAULT TRUE COMMENT 'TRUE if this is the current job',

    -- Salary at that time (for historical reference)
    salary_at_time DECIMAL(15, 2) COMMENT 'Salary when this job was active',

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (position_id) REFERENCES positions(id),
    INDEX idx_employee_id (employee_id),
    INDEX idx_effective_date (effective_date),
    INDEX idx_is_current (is_current),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Salary History Table
-- Tracks all salary changes for an employee
CREATE TABLE IF NOT EXISTS salary_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,

    -- Salary information
    old_salary DECIMAL(15, 2) COMMENT 'Previous salary (NULL for new hire)',
    new_salary DECIMAL(15, 2) NOT NULL COMMENT 'New salary after change',
    salary_difference DECIMAL(15, 2) COMMENT 'Difference (new - old)',

    -- Change details
    change_type VARCHAR(50) NOT NULL COMMENT 'NEW_HIRE, INCREASE, DECREASE, PROMOTION, DEMOTION, ANNUAL_REVIEW, ADJUSTMENT',
    change_reason TEXT,

    -- Effective date
    effective_date DATE NOT NULL COMMENT 'When this salary change takes effect',

    -- Related job change (if any)
    job_history_id BIGINT COMMENT 'Reference to employee_job_history if related to job change',

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT 'Who made this change',

    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (job_history_id) REFERENCES employee_job_history(id) ON DELETE SET NULL,
    INDEX idx_employee_id (employee_id),
    INDEX idx_effective_date (effective_date),
    INDEX idx_change_type (change_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert initial job history for existing employees
-- Create job history records based on current employee data
INSERT INTO employee_job_history (
    employee_id,
    department_id,
    position_id,
    change_type,
    effective_date,
    salary_at_time,
    is_current,
    created_at,
    updated_at
)
SELECT
    e.id as employee_id,
    e.department_id,
    e.position_id,
    'INITIAL' as change_type,
    e.hire_date as effective_date,
    e.basic_salary as salary_at_time,
    TRUE as is_current,
    e.created_at,
    e.updated_at
FROM employees e
WHERE e.deleted_at IS NULL;

-- Insert initial salary history for existing employees
INSERT INTO salary_history (
    employee_id,
    old_salary,
    new_salary,
    salary_difference,
    change_type,
    effective_date,
    job_history_id,
    created_at,
    created_by
)
SELECT
    e.id as employee_id,
    NULL as old_salary,
    e.basic_salary as new_salary,
    e.basic_salary as salary_difference,
    'INITIAL' as change_type,
    e.hire_date as effective_date,
    jh.id as job_history_id,
    e.created_at,
    e.created_by
FROM employees e
INNER JOIN employee_job_history jh ON jh.employee_id = e.id AND jh.is_current = TRUE
WHERE e.deleted_at IS NULL AND e.basic_salary IS NOT NULL;
