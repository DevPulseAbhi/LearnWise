package com.learnwise.servlet;

import com.learnwise.dto.ErrorResponse;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;

public final class HealthServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var services = (AppContextListener.Services) getServletContext().getAttribute(AppContextListener.SERVICES);
        if (services == null || !services.health().isDatabaseAvailable()) {
            JsonSupport.write(response, 503, new ErrorResponse("DATABASE_UNAVAILABLE", "Service is temporarily unavailable", Instant.now(), requestId(request)));
            return;
        }
        JsonSupport.write(response, 200, java.util.Map.of("status", "ok", "database", "ok", "timestamp", Instant.now()));
    }
    private static String requestId(HttpServletRequest request) { return String.valueOf(request.getAttribute("requestId")); }
}
