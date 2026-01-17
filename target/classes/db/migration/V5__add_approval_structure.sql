-- V5: Add Approval Structure
-- Add parent/child relationship for departments, level for positions, and approver for employees

-- =====================================================
-- DEPARTMENTS: Add parent_id and head_id
-- =====================================================
-- Skip if already exists (for idempotent migration)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'departments' AND COLUMN_NAME = 'parent_id');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE departments ADD COLUMN parent_id BIGINT NULL COMMENT ''Parent department ID for hierarchical structure''',
    'SELECT ''Column parent_id already exists in departments''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'departments' AND COLUMN_NAME = 'head_id');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE departments ADD COLUMN head_id BIGINT NULL COMMENT ''Employee ID as head of department''',
    'SELECT ''Column head_id already exists in departments''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key for parent_id (self-referencing)
-- Skip if constraint already exists
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'departments' AND CONSTRAINT_NAME = 'fk_dept_parent');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE departments ADD CONSTRAINT fk_dept_parent FOREIGN KEY (parent_id) REFERENCES departments(id) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT ''Foreign key fk_dept_parent already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key for head_id
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'departments' AND CONSTRAINT_NAME = 'fk_dept_head');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE departments ADD CONSTRAINT fk_dept_head FOREIGN KEY (head_id) REFERENCES employees(id) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT ''Foreign key fk_dept_head already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for parent_id (skip if exists)
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'departments' AND INDEX_NAME = 'idx_dept_parent');
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_dept_parent ON departments(parent_id)',
    'SELECT ''Index idx_dept_parent already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- POSITIONS: Add level field
-- =====================================================
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'positions' AND COLUMN_NAME = 'level');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE positions ADD COLUMN level INT DEFAULT 1 COMMENT ''Position level: 1=Staff, 2=Senior, 3=Supervisor, 4=Manager, 5=Warek/Dekan, 6=Rektor''',
    'SELECT ''Column level already exists in positions''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for level
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'positions' AND INDEX_NAME = 'idx_position_level');
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_position_level ON positions(level)',
    'SELECT ''Index idx_position_level already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- EMPLOYEES: Add approver_id
-- =====================================================
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employees' AND COLUMN_NAME = 'approver_id');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE employees ADD COLUMN approver_id BIGINT NULL COMMENT ''Employee ID for approval (backup approver)''',
    'SELECT ''Column approver_id already exists in employees''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key for approver_id
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'employees' AND CONSTRAINT_NAME = 'fk_employee_approver');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE employees ADD CONSTRAINT fk_employee_approver FOREIGN KEY (approver_id) REFERENCES employees(id) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT ''Foreign key fk_employee_approver already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for approver_id
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employees' AND INDEX_NAME = 'idx_employee_approver');
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_employee_approver ON employees(approver_id)',
    'SELECT ''Index idx_employee_approver already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- LEAVE_REQUESTS: Add current_approver_id only (approved_by and approved_at already exist)
-- =====================================================
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'leave_requests' AND COLUMN_NAME = 'current_approver_id');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE leave_requests ADD COLUMN current_approver_id BIGINT NULL COMMENT ''Current approver in the approval chain''',
    'SELECT ''Column current_approver_id already exists in leave_requests''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key for current_approver_id
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'leave_requests' AND CONSTRAINT_NAME = 'fk_lr_current_approver');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE leave_requests ADD CONSTRAINT fk_lr_current_approver FOREIGN KEY (current_approver_id) REFERENCES employees(id) ON DELETE SET NULL ON UPDATE CASCADE',
    'SELECT ''Foreign key fk_lr_current_approver already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for current_approver_id
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'leave_requests' AND INDEX_NAME = 'idx_lr_current_approver');
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_lr_current_approver ON leave_requests(current_approver_id)',
    'SELECT ''Index idx_lr_current_approver already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- Insert default position levels for existing positions
-- =====================================================
UPDATE positions SET level = 4 WHERE (name LIKE '%Manager%' OR name LIKE '%Head%') AND level IS NULL;
UPDATE positions SET level = 5 WHERE (name LIKE '%Warek%' OR name LIKE '%Dekan%' OR name LIKE '%Director%') AND level IS NULL;
UPDATE positions SET level = 6 WHERE (name LIKE '%Rektor%' OR name LIKE '%President%') AND level IS NULL;
UPDATE positions SET level = 3 WHERE (name LIKE '%Supervisor%' OR name LIKE '%Lead%') AND level IS NULL;
UPDATE positions SET level = 2 WHERE name LIKE '%Senior%' AND level IS NULL;
UPDATE positions SET level = 1 WHERE level IS NULL;
