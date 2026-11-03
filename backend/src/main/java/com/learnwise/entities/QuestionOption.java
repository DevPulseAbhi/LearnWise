package com.learnwise.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "question_options")
public class QuestionOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "question_id") private Question question;
    @Column(name = "option_text", nullable = false) private String text;
    @Column(name = "is_correct", nullable = false) private boolean correct;
    protected QuestionOption() {}
    public QuestionOption(Question question, String text, boolean correct) { this.question = question; this.text = text; this.correct = correct; }
    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public String getText() { return text; }
    public boolean isCorrect() { return correct; }
}
