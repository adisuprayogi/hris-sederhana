-- Add audit columns to leave_types table
ALTER TABLE leave_types ADD COLUMN created_by BIGINT AFTER created_at;
ALTER TABLE leave_types ADD COLUMN updated_by BIGINT AFTER updated_at;
