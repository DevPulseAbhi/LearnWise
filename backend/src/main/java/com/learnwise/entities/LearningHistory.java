package com.learnwise.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "learning_history")
public class LearningHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(name = "topic_id", nullable = false) private Long topicId;
    @Column(name = "activity_type", nullable = false, columnDefinition = "ENUM('QUIZ_STARTED','QUIZ_COMPLETED','LESSON_VIEWED')") private String activityType;
    @Column(name = "reference_id") private Long referenceId;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    protected LearningHistory() {}
    public LearningHistory(Long userId, Long topicId, String activityType, Long referenceId) { this.userId = userId; this.topicId = topicId; this.activityType = activityType; this.referenceId = referenceId; this.occurredAt = Instant.now(); }
}
