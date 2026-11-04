package com.learnwise.servlet;
import com.learnwise.dto.ErrorResponse; import com.learnwise.services.*; import jakarta.servlet.http.*; import java.io.*; import java.time.*; import java.util.*;
public final class PlanningServlet extends HttpServlet{
 private PlanningService planning; private AnalyticsService analytics;
 @Override public void init(){planning=(PlanningService)getServletContext().getAttribute(PlanningService.class.getName());analytics=(AnalyticsService)getServletContext().getAttribute(AnalyticsService.class.getName());}
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse p)throws IOException{var u=current(r).user().getId();var path=r.getPathInfo();if("/analytics".equals(path)){JsonSupport.write(p,200,analytics.summary(u));return;}if("/knowledge-map".equals(path)){JsonSupport.write(p,200,analytics.knowledge(u));return;}JsonSupport.write(p,200,planning.plans(u));}
 @Override protected void doPost(HttpServletRequest r,HttpServletResponse p)throws IOException{try{var u=current(r).user().getId();var path=r.getPathInfo();if(path!=null&&path.matches("/tasks/\\d+")){var b=JsonSupport.read(r,TaskAction.class);JsonSupport.write(p,200,planning.update(u,Long.parseLong(path.substring(7)),b.action(),b.dueAt()));return;}var b=JsonSupport.read(r,PlanRequest.class);JsonSupport.write(p,201,planning.create(u,b.title(),b.goal(),b.deadline(),b.dailyMinutes(),b.topicIds()));}catch(IllegalArgumentException e){JsonSupport.write(p,400,new ErrorResponse("INVALID_PLAN",e.getMessage(),Instant.now(),String.valueOf(r.getAttribute("requestId"))));}}
 private AuthService.AuthenticatedSession current(HttpServletRequest r){return (AuthService.AuthenticatedSession)r.getAttribute(AuthFilter.AUTHENTICATED);}
 public record PlanRequest(String title,String goal,LocalDate deadline,int dailyMinutes,List<Long> topicIds){} public record TaskAction(String action,Instant dueAt){}
}
