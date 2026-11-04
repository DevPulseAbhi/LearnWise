package com.learnwise.entities;
import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="study_tasks")
public class StudyTask {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="plan_id",nullable=false) private Long planId; @Column(name="user_id",nullable=false) private Long userId; @Column(name="topic_id",nullable=false) private Long topicId;
 @Column(name="task_type",nullable=false,columnDefinition="ENUM('PRACTICE','REVIEW','REVISION')") private String taskType;
 @Column(nullable=false,length=180) private String title; @Column(name="due_at",nullable=false) private Instant dueAt;
 @Column(nullable=false,columnDefinition="ENUM('OPEN','COMPLETED','POSTPONED')") private String status;
 @Column(name="completed_at") private Instant completedAt; @Column(name="rescheduled_from") private Instant rescheduledFrom;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 protected StudyTask(){} public StudyTask(Long p,Long u,Long t,String type,String name,Instant due){planId=p;userId=u;topicId=t;taskType=type;title=name;dueAt=due;status="OPEN";}
 @PrePersist void created(){createdAt=Instant.now();} public Long getId(){return id;} public Long getUserId(){return userId;} public Long getPlanId(){return planId;} public Long getTopicId(){return topicId;} public String getTaskType(){return taskType;} public String getTitle(){return title;} public Instant getDueAt(){return dueAt;} public String getStatus(){return status;} public void complete(){status="COMPLETED";completedAt=Instant.now();} public void postpone(Instant from,Instant to){status="POSTPONED";rescheduledFrom=from;dueAt=to;}
}
