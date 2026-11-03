package com.learnwise.repositories;

import com.learnwise.entities.Course;
import org.hibernate.Session;
import org.hibernate.Hibernate;
import java.util.List;
import java.util.Optional;

public final class CourseRepository {
    public Optional<Course> findById(Session session, long id) {
        return Optional.ofNullable(session.get(Course.class, id));
    }
    public List<Course> findPage(Session session, int offset, int limit, String topic) {
        var query = topic == null || topic.isBlank()
                ? session.createQuery("select c from Course c order by c.createdAt desc", Course.class)
                : session.createQuery("select distinct c from Course c join c.topics t where t.slug = :topic order by c.createdAt desc", Course.class)
                    .setParameter("topic", topic);
        var courses = query.setFirstResult(offset).setMaxResults(limit).getResultList();
        courses.forEach(course -> Hibernate.initialize(course.getTopics()));
        return courses;
    }
    public long count(Session session, String topic) {
        var query = topic == null || topic.isBlank()
                ? session.createQuery("select count(c) from Course c", Long.class)
                : session.createQuery("select count(distinct c) from Course c join c.topics t where t.slug = :topic", Long.class).setParameter("topic", topic);
        return query.getSingleResult();
    }
    public void save(Session session, Course course) { session.persist(course); }
}
