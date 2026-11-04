package com.learnwise.entities;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name="study_plans")
public class StudyPlan {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="user_id",nullable=false) private Long userId;
 @Column(nullable=false,length=180) private String title;
 @Column(nullable=false,length=500) private String goal;
 @Column private LocalDate deadline;
 @Column(name="daily_minutes",nullable=false) private int dailyMinutes;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 protected StudyPlan(){}
 public StudyPlan(Long u,String t,String g,LocalDate d,int m){userId=u;title=t;goal=g;deadline=d;dailyMinutes=m;}
 @PrePersist void created(){createdAt=Instant.now();updatedAt=createdAt;} @PreUpdate void changed(){updatedAt=Instant.now();}
 public Long getId(){return id;} public Long getUserId(){return userId;} public String getTitle(){return title;} public String getGoal(){return goal;} public LocalDate getDeadline(){return deadline;} public int getDailyMinutes(){return dailyMinutes;}
}
