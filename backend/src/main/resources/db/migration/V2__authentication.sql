ALTER TABLE users ADD COLUMN provider_subject VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(1000) NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_provider_subject UNIQUE (provider_subject);

CREATE TABLE auth_sessions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash BINARY(32) NOT NULL,
    csrf_token_hash BINARY(32) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL,
    last_seen_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_sessions_token_hash (token_hash),
    KEY ix_auth_sessions_user (user_id),
    KEY ix_auth_sessions_expiry (expires_at),
    CONSTRAINT fk_auth_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
