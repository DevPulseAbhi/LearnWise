package com.learnwise.services;
import com.learnwise.entities.*; import org.hibernate.SessionFactory; import java.time.*; import java.util.*;
public final class PlanningService {
 private final SessionFactory factory; public PlanningService(SessionFactory f){factory=f;}
 public PlanView create(long user,String title,String goal,LocalDate deadline,int dailyMinutes,List<Long> topicIds){
  if(title==null||title.isBlank()||goal==null||goal.isBlank()||dailyMinutes<5||dailyMinutes>1440)throw new IllegalArgumentException("Plan title, goal, and daily availability are required");
  if(topicIds==null||topicIds.isEmpty())throw new IllegalArgumentException("At least one topic is required");
  try(var s=factory.openSession()){var tx=s.beginTransaction(); var p=new StudyPlan(user,title.trim(),goal.trim(),deadline,dailyMinutes);s.persist(p);s.flush();var due=Instant.now();int i=0;
   for(var topic:topicIds){var name=(String)s.createNativeQuery("select name from topics where id=:id").setParameter("id",topic).uniqueResult();if(name==null)throw new IllegalArgumentException("Topic is not available");var masteryRow=s.createNativeQuery("select coalesce(mastery_percent,0) from topic_mastery where user_id=:u and topic_id=:t").setParameter("u",user).setParameter("t",topic).uniqueResult();var mastery=masteryRow==null?0:((Number)masteryRow).doubleValue();var type=mastery<60?"PRACTICE":mastery<80?"REVIEW":"REVISION";s.persist(new StudyTask(p.getId(),user,topic,type,type.substring(0,1)+type.substring(1).toLowerCase()+" · "+name,due.plusSeconds((long)i*86400)));i++;}
   tx.commit();return view(p,s,user);
  }
 }
 public List<PlanView> plans(long user){try(var s=factory.openSession()){return s.createQuery("from StudyPlan p where p.userId=:u order by p.updatedAt desc",StudyPlan.class).setParameter("u",user).getResultList().stream().map(p->view(p,s,user)).toList();}}
 public TaskView update(long user,long id,String action,Instant due){try(var s=factory.openSession()){var tx=s.beginTransaction();var t=s.createQuery("from StudyTask t where t.id=:id and t.userId=:u",StudyTask.class).setParameter("id",id).setParameter("u",user).uniqueResult();if(t==null)throw new IllegalArgumentException("Task not found");if("complete".equals(action)){if(!"COMPLETED".equals(t.getStatus()))t.complete();}else if("postpone".equals(action)){if(due==null||due.isBefore(Instant.now()))throw new IllegalArgumentException("A future due time is required");t.postpone(t.getDueAt(),due);}else throw new IllegalArgumentException("Unsupported task action");tx.commit();return task(t);}}
 private PlanView view(StudyPlan p,org.hibernate.Session s,long u){var tasks=s.createQuery("from StudyTask t where t.planId=:p and t.userId=:u order by t.dueAt",StudyTask.class).setParameter("p",p.getId()).setParameter("u",u).getResultList().stream().map(this::task).toList();return new PlanView(p.getId(),p.getTitle(),p.getGoal(),p.getDeadline(),p.getDailyMinutes(),tasks);}
 private TaskView task(StudyTask t){return new TaskView(t.getId(),t.getTopicId(),t.getTaskType(),t.getTitle(),t.getDueAt(),t.getStatus());}
 public record PlanView(Long id,String title,String goal,LocalDate deadline,int dailyMinutes,List<TaskView> tasks){} public record TaskView(Long id,Long topicId,String type,String title,Instant dueAt,String status){}
}
