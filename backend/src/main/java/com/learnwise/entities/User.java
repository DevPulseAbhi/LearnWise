package com.learnwise.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 254) private String email;
    @Column(name = "display_name", nullable = false, length = 120) private String displayName;
    @Column(name = "avatar_url", length = 1000) private String avatarUrl;
    @Column(name = "password_hash") private String passwordHash;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected User() {}
    public User(String email, String passwordHash, String displayName) {
        this.email = email; this.passwordHash = passwordHash; this.displayName = displayName;
    }
    @PrePersist void created() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void changed() { updatedAt = Instant.now(); }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
