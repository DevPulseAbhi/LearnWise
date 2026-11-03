package com.learnwise.learning;

import com.learnwise.entities.*;
import com.learnwise.services.QuizService;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class QuizServiceTest {
    private org.hibernate.SessionFactory factory;
    private QuizService quizzes;
    private long userId;
    private long topicId;
    private long questionId;
    private long correctOptionId;

    @BeforeEach void setUp() {
        var url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        factory = new Configuration().setProperty(AvailableSettings.JAKARTA_JDBC_URL, url)
                .setProperty(AvailableSettings.JAKARTA_JDBC_USER, "sa").setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, "")
                .setProperty(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                .addAnnotatedClass(User.class).addAnnotatedClass(Topic.class).addAnnotatedClass(Subject.class)
                .addAnnotatedClass(Question.class).addAnnotatedClass(QuestionOption.class).addAnnotatedClass(QuizAttempt.class)
                .addAnnotatedClass(AnswerRecord.class).addAnnotatedClass(TopicMastery.class).addAnnotatedClass(LearningHistory.class)
                .buildSessionFactory();
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            session.createNativeMutationQuery("create table quiz_attempt_questions (attempt_id bigint not null, question_id bigint not null, question_order int not null, primary key (attempt_id, question_id))").executeUpdate();
            var user = new User("learner@example.com", "hashed-password", "Learner");
            var topic = new Topic("Research", "research");
            session.persist(user); session.persist(topic);
            var question = new Question(topic, "Which option is correct?", 1, "The first option is authoritative.");
            session.persist(question);
            var correct = new QuestionOption(question, "Correct", true);
            session.persist(correct); session.persist(new QuestionOption(question, "Incorrect", false));
            tx.commit(); userId = user.getId(); topicId = topic.getId(); questionId = question.getId(); correctOptionId = correct.getId();
        }
        quizzes = new QuizService(factory);
    }

    @AfterEach void tearDown() { factory.close(); }

    @Test void completesQuizScoresServerSideAndUpdatesMastery() {
        var started = quizzes.start(userId, topicId, QuizMode.PRACTICE);
        assertEquals(1, started.questions().size());
        var result = quizzes.answer(userId, started.attemptId(), questionId, correctOptionId);
        assertTrue(result.correct());
        assertEquals("COMPLETED", result.status());
        assertEquals(100.0, result.scorePercent());
        assertEquals(1, quizzes.history(userId, topicId).size());
    }

    @Test void rejectsDuplicateAndCrossAttemptQuestions() {
        var started = quizzes.start(userId, topicId, QuizMode.DIAGNOSTIC);
        quizzes.answer(userId, started.attemptId(), questionId, correctOptionId);
        assertThrows(IllegalStateException.class, () -> quizzes.answer(userId, started.attemptId(), questionId, correctOptionId));
        var second = quizzes.start(userId, topicId, QuizMode.PRACTICE);
        assertThrows(IllegalArgumentException.class, () -> quizzes.answer(userId, second.attemptId(), 9999, correctOptionId));
    }
}
