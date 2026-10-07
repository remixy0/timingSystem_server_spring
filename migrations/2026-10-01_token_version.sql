-- Adds users.token_version, used to revoke login tokens (POST /api/logout-all).
-- Hibernate adds it automatically when ddl-auto=update; run this if production uses validate/none.
-- Safe to run more than once.
ALTER TABLE users ADD COLUMN IF NOT EXISTS token_version integer NOT NULL DEFAULT 0;
