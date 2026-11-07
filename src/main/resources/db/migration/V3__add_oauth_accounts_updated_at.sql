ALTER TABLE oauth_accounts
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();