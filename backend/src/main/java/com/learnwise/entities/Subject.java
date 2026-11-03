package com.learnwise.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 140, unique = true) private String slug;
    protected Subject() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
}
