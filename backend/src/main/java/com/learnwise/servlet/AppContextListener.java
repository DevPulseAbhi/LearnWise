package com.learnwise.servlet;

import com.learnwise.configuration.AppConfig;
import com.learnwise.configuration.PersistenceManager;
import com.learnwise.repositories.CourseRepository;
import com.learnwise.repositories.JdbcHealthRepository;
import com.learnwise.services.CourseService;
import com.learnwise.services.AuthService;
import com.learnwise.configuration.AuthConfig;
import com.learnwise.security.PasswordHasher;
import com.learnwise.services.QuizService;
import jakarta.servlet.*;
import java.util.HashMap;
import com.learnwise.configuration.DocumentConfig;

public final class AppContextListener implements ServletContextListener {
    public static final String SERVICES = AppContextListener.class.getName() + ".services";
    private PersistenceManager persistence;
    @Override public void contextInitialized(ServletContextEvent event) {
        var env = new HashMap<>(System.getenv());
        for (var key : new String[] {
                "LEARNWISE_DB_URL", "LEARNWISE_DB_USER", "LEARNWISE_DB_PASSWORD",
                "LEARNWISE_DB_POOL_SIZE", "LEARNWISE_DB_MIGRATE",
                "LEARNWISE_FRONTEND_ORIGIN", "LEARNWISE_SESSION_TTL_MINUTES",
                "LEARNWISE_COOKIE_SECURE"
        }) {
            env.putIfAbsent(key, System.getProperty(key));
        }
        env.putIfAbsent("LEARNWISE_FRONTEND_ORIGIN", "http://localhost:5173");
        var config = AppConfig.fromEnvironment(env);
        var authConfig = AuthConfig.fromEnvironment(env);
        var documentConfig = DocumentConfig.fromEnvironment(env);
        persistence = new PersistenceManager(config);
        event.getServletContext().setAttribute(AuthConfig.class.getName(), authConfig);
        var authService = new AuthService(persistence.sessionFactory(), new com.learnwise.repositories.UserRepository(),
                new com.learnwise.repositories.AuthSessionRepository(), new PasswordHasher(),
                authConfig.sessionTtlMinutes());
        event.getServletContext().setAttribute(AuthService.class.getName(), authService);
        event.getServletContext().setAttribute(QuizService.class.getName(), new QuizService(persistence.sessionFactory()));
        var documentService = new com.learnwise.services.DocumentService(persistence.sessionFactory(), documentConfig);
        event.getServletContext().setAttribute(com.learnwise.services.DocumentService.class.getName(), documentService);
        event.getServletContext().setAttribute(com.learnwise.services.AiTutorService.class.getName(), new com.learnwise.services.AiTutorService(documentService));
        event.getServletContext().setAttribute(com.learnwise.services.PlanningService.class.getName(), new com.learnwise.services.PlanningService(persistence.sessionFactory()));
        event.getServletContext().setAttribute(com.learnwise.services.AnalyticsService.class.getName(), new com.learnwise.services.AnalyticsService(persistence.sessionFactory()));
        var services = new Services(
                new CourseService(persistence.sessionFactory(), new CourseRepository()),
                new JdbcHealthRepository(persistence.dataSource()));
        event.getServletContext().setAttribute(SERVICES, services);
    }
    @Override public void contextDestroyed(ServletContextEvent event) {
        if (persistence != null) persistence.close();
    }
    public record Services(CourseService courses, JdbcHealthRepository health) {}
}
