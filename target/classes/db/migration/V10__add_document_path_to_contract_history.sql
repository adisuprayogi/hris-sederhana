-- Add document_path column to contract_history table
ALTER TABLE contract_history 
ADD COLUMN document_path VARCHAR(500) NULL 
AFTER notes;

-- Add index for document_path
CREATE INDEX idx_document_path ON contract_history(document_path);
