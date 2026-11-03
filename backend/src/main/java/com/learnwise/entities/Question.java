package com.learnwise.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "topic_id") private Topic topic;
    @Column(nullable = false, columnDefinition = "TEXT") private String prompt;
    @Column(name = "question_type", nullable = false, columnDefinition = "ENUM('SINGLE_CHOICE')") private String questionType;
    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED") private int difficulty;
    @Column(columnDefinition = "TEXT") private String explanation;
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY) private List<QuestionOption> options = new ArrayList<>();
    protected Question() {}
    public Question(Topic topic, String prompt, int difficulty, String explanation) { this.topic = topic; this.prompt = prompt; this.questionType = "SINGLE_CHOICE"; this.difficulty = difficulty; this.explanation = explanation; }
    public Long getId() { return id; }
    public Topic getTopic() { return topic; }
    public String getPrompt() { return prompt; }
    public String getQuestionType() { return questionType; }
    public int getDifficulty() { return difficulty; }
    public String getExplanation() { return explanation; }
    public List<QuestionOption> getOptions() { return options; }
}
