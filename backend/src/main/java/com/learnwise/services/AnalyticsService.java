package com.learnwise.services;
import org.hibernate.SessionFactory; import java.util.*;
public final class AnalyticsService {
 private final SessionFactory factory; public AnalyticsService(SessionFactory f){factory=f;}
 public Map<String,Object> summary(long user){
  try(var s=factory.openSession()){
   var attempts=((Number)s.createNativeQuery("select count(*) from quiz_attempts where user_id=:u and status='COMPLETED'").setParameter("u",user).getSingleResult()).longValue();
   var accuracy=((Number)s.createNativeQuery("select coalesce(avg(score_percent),0) from quiz_attempts where user_id=:u and status='COMPLETED'").setParameter("u",user).getSingleResult()).doubleValue();
   var open=((Number)s.createNativeQuery("select count(*) from study_tasks where user_id=:u and status<>'COMPLETED' and due_at<current_timestamp(6)").setParameter("u",user).getSingleResult()).longValue();
   var mastery=s.createNativeQuery("select t.id,t.name,coalesce(m.mastery_percent,0),coalesce(m.sample_size,0) from topics t left join topic_mastery m on m.topic_id=t.id and m.user_id=:u order by coalesce(m.mastery_percent,0),t.name").setParameter("u",user).getResultList().stream().map(row->{var r=(Object[])row;return Map.of("id",((Number)r[0]).longValue(),"name",r[1],"mastery",((Number)r[2]).doubleValue(),"sampleSize",((Number)r[3]).intValue(),"evidence",((Number)r[3]).intValue()<3?"Insufficient sample":"Based on completed quiz attempts");}).toList();
   return Map.of("completedAttempts",attempts,"accuracyPercent",accuracy,"overdueTasks",open,"mastery",mastery,"hasSufficientEvidence",attempts>0);
  }
 }
 public List<Map<String,Object>> knowledge(long user){try(var s=factory.openSession()){return s.createNativeQuery("select t.id,t.name,t.slug,coalesce(m.mastery_percent,0),coalesce(m.sample_size,0),p.prerequisite_topic_id from topics t left join topic_mastery m on m.topic_id=t.id and m.user_id=:u left join topic_prerequisites p on p.topic_id=t.id order by t.name").setParameter("u",user).getResultList().stream().map(x->{var r=(Object[])x;var result=new LinkedHashMap<String,Object>();result.put("id",((Number)r[0]).longValue());result.put("name",r[1]);result.put("slug",r[2]);result.put("mastery",((Number)r[3]).doubleValue());result.put("sampleSize",((Number)r[4]).intValue());result.put("prerequisiteId",r[5]==null?0:((Number)r[5]).longValue());return result;}).toList();}}
}
