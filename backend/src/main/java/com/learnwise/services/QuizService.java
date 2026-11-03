package com.learnwise.services;

import com.learnwise.entities.*;
import org.hibernate.SessionFactory;
import java.util.List;

public final class QuizService {
    private final SessionFactory factory;
    public QuizService(SessionFactory factory) { this.factory = factory; }

    public QuizView start(long userId, long topicId, QuizMode mode) {
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            try {
                var user = session.get(User.class, userId);
                var topic = session.get(Topic.class, topicId);
                if (user == null || topic == null) throw new IllegalArgumentException("Topic is not available");
                var mastery = session.get(TopicMastery.class, new TopicMastery.Key(userId, topicId));
                var target = mastery == null || mastery.getSampleSize() < 3 ? 1 : mastery.getMasteryPercent() < 60 ? 1 : mastery.getMasteryPercent() < 80 ? 2 : 3;
                var questions = session.createQuery("select q from Question q left join fetch q.options where q.topic.id = :topic and q.difficulty between :low and :high order by q.id", Question.class)
                        .setParameter("topic", topicId).setParameter("low", Math.max(1, target)).setParameter("high", Math.min(5, target + 2))
                        .setMaxResults(5).getResultList();
                if (questions.isEmpty()) throw new IllegalArgumentException("No questions are available for this topic");
                var attempt = new QuizAttempt(user, topic, mode);
                session.persist(attempt);
                session.flush();
                for (int i = 0; i < questions.size(); i++) {
                    session.createNativeMutationQuery("insert into quiz_attempt_questions (attempt_id, question_id, question_order) values (:a, :q, :o)")
                            .setParameter("a", attempt.getId()).setParameter("q", questions.get(i).getId()).setParameter("o", i + 1).executeUpdate();
                }
                session.persist(new LearningHistory(userId, topicId, "QUIZ_STARTED", attempt.getId()));
                tx.commit();
                return view(attempt, questions);
            } catch (RuntimeException e) { if (tx.isActive()) tx.rollback(); throw e; }
        }
    }

    public AnswerResult answer(long userId, long attemptId, long questionId, Long optionId) {
        try (var session = factory.openSession()) {
            var tx = session.beginTransaction();
            try {
                var attempt = session.createQuery("from QuizAttempt a join fetch a.topic where a.id = :id and a.user.id = :user", QuizAttempt.class)
                        .setParameter("id", attemptId).setParameter("user", userId).uniqueResult();
                if (attempt == null) throw new IllegalArgumentException("Attempt not found");
                if (attempt.getStatus() != QuizStatus.IN_PROGRESS) throw new IllegalStateException("Attempt is already complete");
                var assigned = ((Number) session.createNativeQuery("select count(*) from quiz_attempt_questions where attempt_id = :a and question_id = :q")
                        .setParameter("a", attemptId).setParameter("q", questionId).getSingleResult()).longValue();
                if (assigned != 1) throw new IllegalArgumentException("Question is not part of this attempt");
                var duplicate = session.createQuery("select count(a) from AnswerRecord a where a.attempt.id = :a and a.question.id = :q", Long.class)
                        .setParameter("a", attemptId).setParameter("q", questionId).getSingleResult();
                if (duplicate > 0) throw new IllegalStateException("Question has already been answered");
                var question = session.get(Question.class, questionId);
                if (!"SINGLE_CHOICE".equals(question.getQuestionType())) throw new IllegalArgumentException("Unsupported question type");
                var option = optionId == null ? null : session.get(QuestionOption.class, optionId);
                if (option != null && option.getQuestion().getId() != questionId) throw new IllegalArgumentException("Option is not valid for this question");
                var correct = option != null && option.isCorrect();
                session.persist(new AnswerRecord(attempt, question, option, correct));
                var total = ((Number) session.createNativeQuery("select count(*) from quiz_attempt_questions where attempt_id = :a").setParameter("a", attemptId).getSingleResult()).longValue();
                var answered = duplicate + 1;
                if (answered == total) {
                    var right = session.createQuery("select count(a) from AnswerRecord a where a.attempt.id = :a and a.correct = true", Long.class).setParameter("a", attemptId).getSingleResult();
                    var score = (right * 100.0) / total;
                    attempt.complete(score);
                    var mastery = session.get(TopicMastery.class, new TopicMastery.Key(userId, attempt.getTopic().getId()));
                    if (mastery == null) { mastery = new TopicMastery(userId, attempt.getTopic().getId()); session.persist(mastery); }
                    mastery.update(right / (double) total);
                    session.persist(new LearningHistory(userId, attempt.getTopic().getId(), "QUIZ_COMPLETED", attemptId));
                }
                tx.commit();
                return new AnswerResult(correct, question.getExplanation(), attempt.getStatus().name(), attempt.getScorePercent());
            } catch (RuntimeException e) { if (tx.isActive()) tx.rollback(); throw e; }
        }
    }

    public List<AttemptSummary> history(long userId, long topicId) {
        try (var session = factory.openSession()) {
            return session.createQuery("select new com.learnwise.services.QuizService$AttemptSummary(a.id, a.mode, a.status, a.scorePercent, a.startedAt) from QuizAttempt a where a.user.id = :user and a.topic.id = :topic order by a.startedAt desc", AttemptSummary.class)
                    .setParameter("user", userId).setParameter("topic", topicId).getResultList();
        }
    }

    private static QuizView view(QuizAttempt attempt, List<Question> questions) {
        return new QuizView(attempt.getId(), attempt.getTopic().getId(), questions.stream().map(q -> new QuestionView(q.getId(), q.getPrompt(), q.getDifficulty(), q.getOptions().stream().map(o -> new OptionView(o.getId(), o.getText())).toList())).toList());
    }
    public record QuizView(long attemptId, long topicId, List<QuestionView> questions) {}
    public record QuestionView(long id, String prompt, int difficulty, List<OptionView> options) {}
    public record OptionView(long id, String text) {}
    public record AnswerResult(boolean correct, String explanation, String status, Double scorePercent) {}
    public record AttemptSummary(long id, QuizMode mode, QuizStatus status, Double scorePercent, java.time.Instant startedAt) {}
}
