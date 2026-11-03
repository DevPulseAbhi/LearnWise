package com.learnwise.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "topic_mastery")
@IdClass(TopicMastery.Key.class)
public class TopicMastery {
    @Id @Column(name = "user_id") private Long userId;
    @Id @Column(name = "topic_id") private Long topicId;
    @Column(name = "mastery_percent", columnDefinition = "DECIMAL(5,2)") private double masteryPercent;
    @Column(name = "sample_size") private int sampleSize;
    @Column(name = "recent_accuracy", columnDefinition = "DECIMAL(5,2)") private double recentAccuracy;
    @Column(name = "updated_at") private Instant updatedAt;
    @Column(name = "algorithm_version") private String algorithmVersion;
    protected TopicMastery() {}
    public TopicMastery(Long userId, Long topicId) { this.userId = userId; this.topicId = topicId; this.updatedAt = Instant.now(); this.algorithmVersion = "mastery-v1"; }
    public void update(double accuracy) { sampleSize++; recentAccuracy = accuracy * 100; masteryPercent = Math.min(100, (masteryPercent * (sampleSize - 1) + recentAccuracy) / sampleSize); updatedAt = Instant.now(); }
    public double getMasteryPercent() { return masteryPercent; }
    public int getSampleSize() { return sampleSize; }
    public record Key(Long userId, Long topicId) implements Serializable {}
}
