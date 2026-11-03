package com.learnwise.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "topics")
public class Topic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 140, unique = true) private String slug;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected Topic() {}
    public Topic(String name, String slug) { this.name = name; this.slug = slug; }
    @PrePersist void created() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
}
