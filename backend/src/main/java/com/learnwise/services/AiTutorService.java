package com.learnwise.services;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class AiTutorService {
    private final DocumentService documents;
    private final ConcurrentHashMap<Long, Window> windows = new ConcurrentHashMap<>();
    public AiTutorService(DocumentService documents) { this.documents = documents; }
    public TutorReply answer(long userId, String message, Long documentId) {
        if (message == null || message.isBlank() || message.length() > 4000) throw new IllegalArgumentException("Message must be between 1 and 4000 characters");
        var now = System.currentTimeMillis();
        var window = windows.compute(userId, (key, current) -> current == null || now - current.startedAt() >= 60_000
                ? new Window(now, 0) : current);
        if (window.requests() >= 20) throw new IllegalStateException("Tutor rate limit reached; try again shortly");
        windows.put(userId, new Window(window.startedAt(), window.requests() + 1));
        var citations = documents.retrieve(userId, message, documentId);
        if (citations.isEmpty()) return new TutorReply("I could not find supporting evidence in your uploaded documents. Please ask about a topic covered by a ready document, or add more context.", List.of(), "mock-v1");
        return new TutorReply("Here is what your document says: " + citations.getFirst().excerpt(), citations, "mock-v1");
    }
    private record Window(long startedAt, int requests) {}
    public record TutorReply(String answer, List<DocumentService.Citation> citations, String provider) {}
}
