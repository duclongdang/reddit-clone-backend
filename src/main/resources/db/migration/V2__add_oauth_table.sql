-- ============================================================
-- Reddit Clone - Add OAuth2 Support
-- Flyway migration: V2__add_oauth_support.sql
-- ============================================================

-- User đăng nhập qua OAuth2 (Google...) sẽ không có password nội bộ
ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;

-- Tách riêng "danh tính đăng nhập" khỏi "thông tin người dùng":
-- 1 user có thể link nhiều provider (Google, GitHub...) cùng lúc,
-- thay vì giới hạn users.provider chỉ chứa được 1 giá trị.
CREATE TABLE oauth_accounts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider        VARCHAR(20) NOT NULL CHECK (provider IN ('GOOGLE', 'GITHUB', 'FACEBOOK')),
    provider_id     VARCHAR(255) NOT NULL,   -- Google's "sub" — ổn định, không dùng email vì email có thể đổi
    email           VARCHAR(255),             -- email do provider trả về, có thể khác users.email
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Chặn 2 user khác nhau cùng link 1 tài khoản Google giống nhau
    CONSTRAINT uq_provider_account UNIQUE (provider, provider_id)
);

CREATE INDEX idx_oauth_accounts_user ON oauth_accounts (user_id);