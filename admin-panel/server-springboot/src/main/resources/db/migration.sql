ALTER TABLE dish ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'approved';
UPDATE dish SET status = 'approved' WHERE status IS NULL;
