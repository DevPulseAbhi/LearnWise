package com.learnwise.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "topic_id") private Topic topic;
    @Enumerated(EnumType.STRING) @Column(nullable = false, columnDefinition = "ENUM('DIAGNOSTIC','PRACTICE')") private QuizMode mode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, columnDefinition = "ENUM('IN_PROGRESS','COMPLETED','ABANDONED')") private QuizStatus status;
    @Column(name = "score_percent", columnDefinition = "DECIMAL(5,2)") private Double scorePercent;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "algorithm_version", nullable = false) private String algorithmVersion;
    @OneToMany(mappedBy = "attempt", fetch = FetchType.LAZY) private List<AnswerRecord> answers = new ArrayList<>();
    protected QuizAttempt() {}
    public QuizAttempt(User user, Topic topic, QuizMode mode) { this.user = user; this.topic = topic; this.mode = mode; this.status = QuizStatus.IN_PROGRESS; this.startedAt = Instant.now(); this.algorithmVersion = "mastery-v1"; }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Topic getTopic() { return topic; }
    public QuizMode getMode() { return mode; }
    public QuizStatus getStatus() { return status; }
    public Double getScorePercent() { return scorePercent; }
    public List<AnswerRecord> getAnswers() { return answers; }
    public void complete(double score) { status = QuizStatus.COMPLETED; scorePercent = score; completedAt = Instant.now(); }
}
