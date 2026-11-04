package com.learnwise.configuration;

import com.learnwise.entities.Course;
import com.learnwise.entities.Topic;
import com.learnwise.entities.User;
import com.learnwise.entities.AuthSession;
import com.learnwise.entities.Subject;
import com.learnwise.entities.Question;
import com.learnwise.entities.QuestionOption;
import com.learnwise.entities.QuizAttempt;
import com.learnwise.entities.AnswerRecord;
import com.learnwise.entities.TopicMastery;
import com.learnwise.entities.LearningHistory;
import com.learnwise.entities.StudyDocument;
import com.learnwise.entities.DocumentChunk;
import com.learnwise.entities.StudyPlan;
import com.learnwise.entities.StudyTask;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;
import java.util.Objects;

public final class PersistenceManager implements AutoCloseable {
    private final HikariDataSource dataSource;
    private final SessionFactory sessionFactory;

    public PersistenceManager(AppConfig config) {
        var poolConfig = new HikariConfig();
        poolConfig.setJdbcUrl(config.jdbcUrl());
        poolConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        poolConfig.setUsername(config.dbUser());
        poolConfig.setPassword(config.dbPassword());
        poolConfig.setMaximumPoolSize(config.poolSize());
        poolConfig.setMinimumIdle(1);
        poolConfig.setPoolName("learnwise-pool");
        this.dataSource = new HikariDataSource(poolConfig);
        if (config.migrate()) Flyway.configure().dataSource(dataSource).load().migrate();

        var hibernate = new Configuration()
                .setProperty(AvailableSettings.JAKARTA_JDBC_URL, config.jdbcUrl())
                .setProperty(AvailableSettings.JAKARTA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver")
                .setProperty(AvailableSettings.JAKARTA_JDBC_USER, config.dbUser())
                .setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, config.dbPassword())
                .setProperty(AvailableSettings.HBM2DDL_AUTO, "validate")
                .setProperty(AvailableSettings.SHOW_SQL, "false")
                .setProperty("hibernate.jdbc.time_zone", "UTC")
                .addAnnotatedClass(Course.class)
                .addAnnotatedClass(Topic.class)
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(AuthSession.class)
                .addAnnotatedClass(Subject.class).addAnnotatedClass(Question.class)
                .addAnnotatedClass(QuestionOption.class).addAnnotatedClass(QuizAttempt.class)
                .addAnnotatedClass(AnswerRecord.class).addAnnotatedClass(TopicMastery.class)
                .addAnnotatedClass(LearningHistory.class).addAnnotatedClass(StudyDocument.class)
                .addAnnotatedClass(DocumentChunk.class).addAnnotatedClass(StudyPlan.class)
                .addAnnotatedClass(StudyTask.class);
        this.sessionFactory = hibernate.buildSessionFactory();
    }

    public SessionFactory sessionFactory() { return sessionFactory; }
    public DataSource dataSource() { return dataSource; }
    public void close() {
        if (sessionFactory.isOpen()) sessionFactory.close();
        if (!dataSource.isClosed()) dataSource.close();
    }
}
