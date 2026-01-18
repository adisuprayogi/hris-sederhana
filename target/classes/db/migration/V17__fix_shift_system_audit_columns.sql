-- =====================================================
-- V17: Fix Audit Columns for Shift System Tables
-- Fix missing created_by, updated_by, deleted_by columns
-- Fix shift_type ENUM type
-- =====================================================

-- Fix working_hours: Add missing created_by and updated_by columns
ALTER TABLE working_hours ADD COLUMN created_by BIGINT NULL AFTER deleted_at;
ALTER TABLE working_hours ADD COLUMN updated_by BIGINT NULL AFTER created_by;

-- Fix shift_packages: Add missing created_by and updated_by columns
ALTER TABLE shift_packages ADD COLUMN created_by BIGINT NULL AFTER deleted_at;
ALTER TABLE shift_packages ADD COLUMN updated_by BIGINT NULL AFTER created_by;

-- Fix shift_patterns: Add missing created_by and updated_by columns
ALTER TABLE shift_patterns ADD COLUMN created_by BIGINT NULL AFTER deleted_at;
ALTER TABLE shift_patterns ADD COLUMN updated_by BIGINT NULL AFTER created_by;

-- Fix shift_patterns: Change shift_type to proper ENUM type
ALTER TABLE shift_patterns MODIFY COLUMN shift_type ENUM('FIXED', 'FLEXIBLE', 'ROTATING') DEFAULT 'FIXED';

-- Fix employee_shift_settings: Add missing deleted_by and updated_by columns
ALTER TABLE employee_shift_settings ADD COLUMN deleted_by BIGINT NULL AFTER deleted_at;
ALTER TABLE employee_shift_settings ADD COLUMN updated_by BIGINT NULL AFTER updated_at;

-- Fix employee_shift_schedules: Add missing updated_by and deleted_by columns
ALTER TABLE employee_shift_schedules ADD COLUMN updated_by BIGINT NULL AFTER updated_at;
ALTER TABLE employee_shift_schedules ADD COLUMN deleted_by BIGINT NULL AFTER deleted_at;
