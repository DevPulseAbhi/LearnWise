CREATE TABLE study_documents (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT UNSIGNED NOT NULL,
    page_count INT UNSIGNED NULL,
    status ENUM('PROCESSING','READY','FAILED') NOT NULL,
    failure_code VARCHAR(60) NULL,
    retry_count INT UNSIGNED NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_documents_storage_key (storage_key),
    KEY ix_documents_user_updated (user_id, updated_at),
    CONSTRAINT fk_documents_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE document_chunks (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    document_id BIGINT UNSIGNED NOT NULL,
    chunk_index INT UNSIGNED NOT NULL,
    page_start INT UNSIGNED NOT NULL,
    page_end INT UNSIGNED NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_document_chunk_index (document_id, chunk_index),
    KEY ix_chunks_document_page (document_id, page_start),
    CONSTRAINT fk_chunks_document FOREIGN KEY (document_id) REFERENCES study_documents(id) ON DELETE CASCADE
) ENGINE=InnoDB;
