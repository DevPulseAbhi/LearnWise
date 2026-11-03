package com.learnwise.persistence;

import com.learnwise.entities.Course;
import com.learnwise.repositories.CourseRepository;
import com.learnwise.repositories.JdbcHealthRepository;
import com.learnwise.services.CourseService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PersistenceIntegrationTest {
    private HikariDataSource dataSource;
    private org.hibernate.SessionFactory sessionFactory;
    private CourseService service;

    @BeforeEach void setUp() {
        var url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        var pool = new HikariConfig(); pool.setJdbcUrl(url); pool.setUsername("sa"); pool.setPassword("");
        dataSource = new HikariDataSource(pool);
        sessionFactory = new Configuration()
                .setProperty(AvailableSettings.JAKARTA_JDBC_URL, url)
                .setProperty(AvailableSettings.JAKARTA_JDBC_USER, "sa")
                .setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, "")
                .setProperty(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                .addAnnotatedClass(Course.class).addAnnotatedClass(com.learnwise.entities.Topic.class)
                .buildSessionFactory();
        service = new CourseService(sessionFactory, new CourseRepository());
    }

    @AfterEach void tearDown() {
        sessionFactory.close();
        dataSource.close();
    }

    @Test void persistsAndPagesCourses() {
        var created = service.create("A course", "A useful description");
        var page = service.findPage(0, 10, null);
        assertEquals(created.getId(), page.items().getFirst().getId());
        assertEquals(1, page.total());
    }

    @Test void rollsBackInvalidTransactionWithoutPersistingPartialData() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.persist(new Course("will rollback", "not committed"));
            transaction.rollback();
        }
        assertEquals(0, service.findPage(0, 10, null).total());
    }

    @Test void jdbcHealthUsesConfiguredDataSource() {
        assertTrue(new JdbcHealthRepository(dataSource).isDatabaseAvailable());
    }

    @Test void persistsCourseTopicRelationship() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var topic = new com.learnwise.entities.Topic("Design", "design");
            var course = new Course("Design course", "A course with a topic");
            course.getTopics().add(topic);
            session.persist(topic);
            session.persist(course);
            transaction.commit();
            session.clear();
            var loaded = session.get(Course.class, course.getId());
            assertEquals("design", loaded.getTopics().iterator().next().getSlug());
        }
    }
}
