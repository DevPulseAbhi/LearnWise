package com.learnwise.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_sessions")
public class AuthSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "token_hash", nullable = false, unique = true, columnDefinition = "BINARY(32)") private byte[] tokenHash;
    @Column(name = "csrf_token_hash", nullable = false, columnDefinition = "BINARY(32)") private byte[] csrfTokenHash;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "last_seen_at", nullable = false) private Instant lastSeenAt;
    protected AuthSession() {}
    public AuthSession(User user, byte[] tokenHash, byte[] csrfTokenHash, Instant expiresAt) {
        this.user = user; this.tokenHash = tokenHash; this.csrfTokenHash = csrfTokenHash; this.expiresAt = expiresAt;
    }
    @PrePersist void created() { createdAt = Instant.now(); lastSeenAt = createdAt; }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public byte[] getCsrfTokenHash() { return csrfTokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void touch() { lastSeenAt = Instant.now(); }
}
