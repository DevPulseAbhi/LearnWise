package com.learnwise.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "document_id", nullable = false) private Long documentId;
    @Column(name = "chunk_index", nullable = false) private int chunkIndex;
    @Column(name = "page_start", nullable = false) private int pageStart;
    @Column(name = "page_end", nullable = false) private int pageEnd;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected DocumentChunk() {}
    public DocumentChunk(Long documentId, int index, int start, int end, String content) {
        this.documentId = documentId; chunkIndex = index; pageStart = start; pageEnd = end; this.content = content;
    }
    @PrePersist void created() { createdAt = Instant.now(); }
    public Long getId() { return id; } public int getChunkIndex() { return chunkIndex; }
    public Long getDocumentId() { return documentId; }
    public int getPageStart() { return pageStart; } public int getPageEnd() { return pageEnd; }
    public String getContent() { return content; }
}
