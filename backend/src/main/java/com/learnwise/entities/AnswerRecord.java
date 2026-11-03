package com.learnwise.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "answer_records", uniqueConstraints = @UniqueConstraint(name = "uk_answer_attempt_question", columnNames = {"attempt_id", "question_id"}))
public class AnswerRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "attempt_id") private QuizAttempt attempt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "question_id") private Question question;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "selected_option_id") private QuestionOption selectedOption;
    @Column(name = "is_correct", nullable = false) private boolean correct;
    @Column(name = "answered_at", nullable = false) private Instant answeredAt;
    protected AnswerRecord() {}
    public AnswerRecord(QuizAttempt attempt, Question question, QuestionOption selectedOption, boolean correct) { this.attempt = attempt; this.question = question; this.selectedOption = selectedOption; this.correct = correct; this.answeredAt = Instant.now(); }
    public boolean isCorrect() { return correct; }
    public Question getQuestion() { return question; }
    public QuestionOption getSelectedOption() { return selectedOption; }
}
