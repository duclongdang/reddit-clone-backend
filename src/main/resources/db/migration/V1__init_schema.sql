-- ============================================================
-- Reddit Clone - Full Schema
-- Flyway migration: V1__init_schema.sql
-- Target: PostgreSQL 16
-- ============================================================

-- ============================================================
-- 1. CORE: users, communities, posts, comments, votes
-- ============================================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(30) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_profiles (
    user_id         BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    display_name    VARCHAR(50),
    bio             TEXT,
    avatar_url      VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE communities (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,
    description     TEXT,
    created_by      BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE posts (
    id              BIGSERIAL PRIMARY KEY,
    author_id       BIGINT NOT NULL REFERENCES users(id),
    community_id    BIGINT NOT NULL REFERENCES communities(id),
    title           VARCHAR(300) NOT NULL,
    content         TEXT,
    score           INTEGER NOT NULL DEFAULT 0,
    upvotes         INTEGER NOT NULL DEFAULT 0,
    downvotes       INTEGER NOT NULL DEFAULT 0,
    hot_score       DOUBLE PRECISION NOT NULL DEFAULT 0,
    view_count      INTEGER NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE comments (
    id              BIGSERIAL PRIMARY KEY,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id       BIGINT NOT NULL REFERENCES users(id),
    parent_id       BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    score           INTEGER NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE post_votes (
    id              BIGSERIAL PRIMARY KEY,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    value           SMALLINT NOT NULL CHECK (value IN (-1, 1)),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_post_vote UNIQUE (user_id, post_id)
);

CREATE TABLE comment_votes (
    id              BIGSERIAL PRIMARY KEY,
    comment_id      BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    value           SMALLINT NOT NULL CHECK (value IN (-1, 1)),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_comment_vote UNIQUE (user_id, comment_id)
);

-- ============================================================
-- 2. QUAN HE & PHAN QUYEN
-- ============================================================

CREATE TABLE community_members (
    id              BIGSERIAL PRIMARY KEY,
    community_id    BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_community_member UNIQUE (community_id, user_id)
);

CREATE TABLE community_moderators (
    id              BIGSERIAL PRIMARY KEY,
    community_id    BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL DEFAULT 'moderator',
    assigned_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_community_moderator UNIQUE (community_id, user_id)
);

CREATE TABLE bans (
    id              BIGSERIAL PRIMARY KEY,
    community_id    BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    banned_by       BIGINT NOT NULL REFERENCES users(id),
    reason          TEXT,
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_community_ban UNIQUE (community_id, user_id)
);

-- ============================================================
-- 3. TUONG TAC & UX
-- ============================================================

CREATE TABLE saved_posts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_saved_post UNIQUE (user_id, post_id)
);

-- actor_id: ai gây ra sự kiện (vd: user trả lời comment của bạn)
-- target_type + target_id: sự kiện liên quan đến đối tượng nào (POST, COMMENT...)
-- Tách riêng thay vì chỉ nhét vào payload để có thể JOIN/filter bằng SQL thường
-- (vd: "tất cả thông báo về post X"), payload JSONB vẫn giữ lại cho phần hiển thị linh động
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id        BIGINT REFERENCES users(id) ON DELETE SET NULL,
    type            VARCHAR(30) NOT NULL,
    target_type     VARCHAR(20) CHECK (target_type IN ('POST', 'COMMENT', 'COMMUNITY', 'USER')),
    target_id       BIGINT,
    payload         JSONB,
    is_read         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE subscriptions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('COMMUNITY', 'USER')),
    target_id       BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_subscription UNIQUE (user_id, target_type, target_id)
);

CREATE TABLE post_views (
    id              BIGSERIAL PRIMARY KEY,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id         BIGINT REFERENCES users(id) ON DELETE SET NULL,
    viewed_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE award_types (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,
    icon_url        VARCHAR(500),
    cost            INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE post_awards (
    id              BIGSERIAL PRIMARY KEY,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    award_type_id   BIGINT NOT NULL REFERENCES award_types(id),
    given_by        BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- 4. NOI DUNG MO RONG
-- ============================================================

CREATE TABLE tags (
    id              BIGSERIAL PRIMARY KEY,
    community_id    BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    name            VARCHAR(30) NOT NULL,
    color           VARCHAR(7),
    CONSTRAINT uq_community_tag UNIQUE (community_id, name)
);

CREATE TABLE post_tags (
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag_id          BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE media (
    id              BIGSERIAL PRIMARY KEY,
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('POST', 'COMMENT')),
    target_id       BIGINT NOT NULL,
    url             VARCHAR(500) NOT NULL,
    media_type      VARCHAR(20) NOT NULL CHECK (media_type IN ('IMAGE', 'VIDEO', 'GIF')),
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE comment_edits (
    id                  BIGSERIAL PRIMARY KEY,
    comment_id          BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    previous_content    TEXT NOT NULL,
    edited_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- 5. VAN HANH & BAO MAT
-- ============================================================

-- Polymorphic association (target_type + target_id) thay vì tách post_id/comment_id/user_id
-- riêng: vì report có thể nhắm vào POST, COMMENT, hoặc USER, tách cột FK riêng sẽ cần 3 cột
-- nullable phức tạp hơn. Đánh đổi: Postgres không có FK constraint thật cho pattern này,
-- nên phải validate target_id tồn tại ở tầng application (service layer), không phải DB.
-- Đây cũng là cách Reddit thật xử lý report đa loại đối tượng.
CREATE TABLE reports (
    id              BIGSERIAL PRIMARY KEY,
    reporter_id     BIGINT NOT NULL REFERENCES users(id),
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('POST', 'COMMENT', 'USER')),
    target_id       BIGINT NOT NULL,
    reason          VARCHAR(50) NOT NULL,
    details         TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWED', 'DISMISSED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    actor_id        BIGINT NOT NULL REFERENCES users(id),
    action          VARCHAR(50) NOT NULL,
    target_type     VARCHAR(20),
    target_id       BIGINT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()

);

-- ============================================================
-- INDEXES
-- ============================================================

-- Feed sorting (Hot / New / Top)
CREATE INDEX idx_posts_hot_score ON posts (community_id, hot_score DESC);
CREATE INDEX idx_posts_created_at ON posts (community_id, created_at DESC);
CREATE INDEX idx_posts_score ON posts (community_id, score DESC);
CREATE INDEX idx_posts_author ON posts (author_id);

-- Nested comments
CREATE INDEX idx_comments_post_id ON comments (post_id);
CREATE INDEX idx_comments_parent_id ON comments (parent_id);

-- Vote lookups
CREATE INDEX idx_post_votes_post_id ON post_votes (post_id);
CREATE INDEX idx_post_votes_user_id ON post_votes (user_id);
CREATE INDEX idx_comment_votes_comment_id ON comment_votes (comment_id);
CREATE INDEX idx_comment_votes_user_id ON comment_votes (user_id);

-- Membership / moderation
CREATE INDEX idx_community_members_user ON community_members (user_id);
CREATE INDEX idx_community_moderators_user ON community_moderators (user_id);
CREATE INDEX idx_bans_user ON bans (user_id);

-- Notifications (unread lookup is hot path)
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, is_read, created_at DESC);
CREATE INDEX idx_notifications_target ON notifications (target_type, target_id);

-- Reports (moderation queue)
CREATE INDEX idx_reports_status ON reports (status, created_at DESC);
CREATE INDEX idx_reports_target ON reports (target_type, target_id);

-- Media / polymorphic lookups
CREATE INDEX idx_media_target ON media (target_type, target_id);

-- Refresh tokens
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

-- Post views (analytics, avoid unbounded growth scans)
CREATE INDEX idx_post_views_post_id ON post_views (post_id);