package com.learnwise.services;

import com.learnwise.entities.Course;
import com.learnwise.repositories.CourseRepository;
import org.hibernate.SessionFactory;
import java.util.List;

public final class CourseService {
    private final SessionFactory sessionFactory;
    private final CourseRepository repository;
    public CourseService(SessionFactory sessionFactory, CourseRepository repository) { this.sessionFactory = sessionFactory; this.repository = repository; }
    public Page<Course> findPage(int page, int size, String topic) {
        validatePage(page, size);
        try (var session = sessionFactory.openSession()) {
            return new Page<>(repository.findPage(session, page * size, size, topic), page, size, repository.count(session, topic));
        }
    }
    public Course create(String title, String description) {
        if (title == null || title.isBlank() || title.length() > 180) throw new IllegalArgumentException("title must be between 1 and 180 characters");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("description is required");
        var course = new Course(title.trim(), description.trim());
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try { repository.save(session, course); transaction.commit(); return course; }
            catch (RuntimeException exception) { if (transaction.isActive()) transaction.rollback(); throw exception; }
        }
    }
    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be >= 0 and size must be between 1 and 100");
    }
    public record Page<T>(List<T> items, int page, int size, long total) {}
}
