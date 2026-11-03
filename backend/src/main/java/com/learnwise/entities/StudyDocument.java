package com.learnwise.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "study_documents")
public class StudyDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "original_name", nullable = false, length = 255) private String originalName;
    @Column(name = "storage_key", nullable = false, length = 512, unique = true) private String storageKey;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @Column(name = "page_count") private Integer pageCount;
    @Column(nullable = false, columnDefinition = "ENUM('PROCESSING','READY','FAILED')") private String status;
    @Column(name = "failure_code", length = 60) private String failureCode;
    @Column(name = "retry_count", nullable = false) private int retryCount;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected StudyDocument() {}
    public StudyDocument(Long userId, String originalName, String storageKey, long fileSize) {
        this.userId = userId; this.originalName = originalName; this.storageKey = storageKey;
        this.contentType = "application/pdf"; this.fileSize = fileSize; this.status = "PROCESSING";
    }
    @PrePersist void created() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void changed() { updatedAt = Instant.now(); }
    public Long getId() { return id; } public Long getUserId() { return userId; }
    public String getOriginalName() { return originalName; } public String getStorageKey() { return storageKey; }
    public long getFileSize() { return fileSize; } public Integer getPageCount() { return pageCount; }
    public String getStatus() { return status; } public String getFailureCode() { return failureCode; }
    public int getRetryCount() { return retryCount; }
    public void ready(int pages) { status = "READY"; pageCount = pages; failureCode = null; }
    public void failed(String code) { status = "FAILED"; failureCode = code; retryCount++; }
    public void processing() { status = "PROCESSING"; failureCode = null; }
}
