-- Add end_date column to salary_history table
ALTER TABLE salary_history
ADD COLUMN end_date DATE NULL COMMENT 'End date of this salary period (NULL if current)';

-- Add index for better querying
CREATE INDEX idx_salary_history_end_date ON salary_history(end_date);
