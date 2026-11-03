ALTER TABLE users DROP INDEX uk_users_provider_subject;
ALTER TABLE users DROP COLUMN provider_subject;
ALTER TABLE users MODIFY password_hash VARCHAR(255) NOT NULL;
