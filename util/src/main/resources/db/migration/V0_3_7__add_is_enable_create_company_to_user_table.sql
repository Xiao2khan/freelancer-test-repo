ALTER TABLE users ADD COLUMN is_enable_create_company BOOLEAN DEFAULT TRUE;

UPDATE users SET is_enable_create_company = TRUE WHERE is_enable_create_company IS NULL;
