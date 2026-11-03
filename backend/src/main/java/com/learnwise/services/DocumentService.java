package com.learnwise.services;

import com.learnwise.configuration.DocumentConfig;
import com.learnwise.entities.DocumentChunk;
import com.learnwise.entities.StudyDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.hibernate.SessionFactory;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class DocumentService {
    private final SessionFactory factory;
    private final DocumentConfig config;
    public DocumentService(SessionFactory factory, DocumentConfig config) { this.factory = factory; this.config = config; }

    public DocumentView upload(long userId, String name, String contentType, byte[] bytes) {
        validate(name, contentType, bytes);
        var key = UUID.randomUUID() + ".pdf";
        var target = safePath(key);
        try {
            Files.createDirectories(config.storageRoot());
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            try (var pdf = Loader.loadPDF(bytes)) {
                var pages = extract(pdf);
                try (var session = factory.openSession()) {
                    var tx = session.beginTransaction();
                    var document = new StudyDocument(userId, cleanName(name), key, bytes.length);
                    session.persist(document); session.flush();
                    persistChunks(session, document.getId(), pages);
                    document.ready(pdf.getNumberOfPages());
                    tx.commit();
                    return view(document);
                }
            }
        } catch (IOException | RuntimeException exception) {
            deleteQuietly(target);
            throw new IllegalArgumentException("The PDF could not be processed safely", exception);
        }
    }

    public List<DocumentView> list(long userId) {
        try (var session = factory.openSession()) {
            return session.createQuery("from StudyDocument d where d.userId = :user order by d.updatedAt desc", StudyDocument.class)
                    .setParameter("user", userId).getResultList().stream().map(DocumentService::view).toList();
        }
    }

    public DocumentView retry(long userId, long id) {
        var existing = owned(userId, id);
        if (!"FAILED".equals(existing.getStatus())) throw new IllegalStateException("Only failed documents can be retried");
        var path = safePath(existing.getStorageKey());
        try {
            var bytes = Files.readAllBytes(path);
            validate(existing.getOriginalName(), "application/pdf", bytes);
            try (var pdf = Loader.loadPDF(bytes); var session = factory.openSession()) {
                var tx = session.beginTransaction();
                session.createMutationQuery("delete from DocumentChunk c where c.documentId = :id").setParameter("id", id).executeUpdate();
                persistChunks(session, id, extract(pdf));
                existing.processing(); existing.ready(pdf.getNumberOfPages());
                session.merge(existing); tx.commit(); return view(existing);
            }
        } catch (IOException | RuntimeException exception) {
            markFailed(userId, id, "PROCESSING_FAILED");
            throw new IllegalArgumentException("The document could not be reprocessed", exception);
        }
    }

    public void delete(long userId, long id) {
        var document = owned(userId, id);
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction(); session.remove(session.merge(document)); tx.commit();
        }
        deleteQuietly(safePath(document.getStorageKey()));
    }

    public List<Citation> retrieve(long userId, String query, Long documentId) {
        if (query == null || query.isBlank()) return List.of();
        try (var session = factory.openSession()) {
            var hql = "select c from DocumentChunk c, StudyDocument d where c.documentId = d.id and d.userId = :user and d.status = 'READY' and lower(c.content) like :query" +
                    (documentId == null ? "" : " and d.id = :document");
            var queryObject = session.createQuery(hql, DocumentChunk.class).setParameter("user", userId).setParameter("query", "%" + query.trim().toLowerCase() + "%");
            if (documentId != null) queryObject.setParameter("document", documentId);
            return queryObject.setMaxResults(5).getResultList().stream().map(c -> new Citation(c.getDocumentId(), c.getPageStart(), c.getPageEnd(), c.getContent())).toList();
        }
    }

    private StudyDocument owned(long userId, long id) {
        try (var session = factory.openSession()) {
            var doc = session.createQuery("from StudyDocument d where d.id = :id and d.userId = :user", StudyDocument.class)
                    .setParameter("id", id).setParameter("user", userId).uniqueResult();
            if (doc == null) throw new IllegalArgumentException("Document not found");
            return doc;
        }
    }
    private void markFailed(long userId, long id, String code) {
        try (var session = factory.openSession()) { var tx = session.beginTransaction(); var d = session.createQuery("from StudyDocument d where d.id=:id and d.userId=:user", StudyDocument.class).setParameter("id", id).setParameter("user", userId).uniqueResult(); if (d != null) { d.failed(code); session.merge(d); } tx.commit(); }
    }
    private List<PageText> extract(org.apache.pdfbox.pdmodel.PDDocument pdf) throws IOException {
        var result = new ArrayList<PageText>(); var stripper = new PDFTextStripper();
        for (int page = 1; page <= pdf.getNumberOfPages(); page++) { stripper.setStartPage(page); stripper.setEndPage(page); result.add(new PageText(page, stripper.getText(pdf))); }
        return result;
    }
    private void persistChunks(org.hibernate.Session session, long id, List<PageText> pages) {
        var text = new StringBuilder(); var starts = new ArrayList<Integer>();
        for (var page : pages) { starts.add(text.length()); text.append(page.text()).append("\n"); }
        var step = config.chunkSize() - config.chunkOverlap(); int index = 0;
        for (int start = 0; start < text.length(); start += step) {
            var end = Math.min(text.length(), start + config.chunkSize()); var pageStart = pageAt(starts, start); var pageEnd = pageAt(starts, Math.max(start, end - 1));
            session.persist(new DocumentChunk(id, index++, pageStart, pageEnd, text.substring(start, end).trim())); if (end == text.length()) break;
        }
    }
    private int pageAt(List<Integer> starts, int offset) { var page = 1; for (int i = 0; i < starts.size(); i++) if (starts.get(i) <= offset) page = i + 1; return page; }
    private Path safePath(String key) { var path = config.storageRoot().resolve(key).normalize(); if (!path.startsWith(config.storageRoot()) || !key.matches("[a-f0-9-]+\\.pdf")) throw new IllegalArgumentException("Invalid document storage key"); return path; }
    private void validate(String name, String type, byte[] bytes) { if (bytes == null || bytes.length == 0 || bytes.length > config.maxBytes()) throw new IllegalArgumentException("PDF size is invalid"); if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".pdf")) throw new IllegalArgumentException("Only .pdf files are accepted"); if (!"application/pdf".equalsIgnoreCase(type)) throw new IllegalArgumentException("PDF MIME type is required"); if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F' || bytes[4] != '-') throw new IllegalArgumentException("PDF signature is invalid"); }
    private String cleanName(String name) { return Path.of(name).getFileName().toString(); }
    private void deleteQuietly(Path path) { try { Files.deleteIfExists(path); } catch (IOException ignored) { } }
    private static DocumentView view(StudyDocument d) { return new DocumentView(d.getId(), d.getOriginalName(), d.getFileSize(), d.getPageCount(), d.getStatus(), d.getFailureCode(), d.getRetryCount()); }
    private record PageText(int page, String text) {}
    public record DocumentView(Long id, String name, long size, Integer pages, String status, String failureCode, int retryCount) {}
    public record Citation(Long documentId, int pageStart, int pageEnd, String excerpt) {}
}
