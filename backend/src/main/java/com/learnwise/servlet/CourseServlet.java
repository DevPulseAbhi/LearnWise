package com.learnwise.servlet;

import com.learnwise.dto.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public final class CourseServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var services = services();
        try {
            int page = parseInteger(request.getParameter("page"), 0);
            int size = parseInteger(request.getParameter("size"), 20);
            var result = services.courses().findPage(page, size, request.getParameter("topic"));
            JsonSupport.write(response, 200, Map.of("items", result.items().stream().map(CourseResponse::from).toList(),
                    "page", result.page(), "size", result.size(), "total", result.total()));
        } catch (IllegalArgumentException exception) {
            JsonSupport.write(response, 400, error("INVALID_REQUEST", exception.getMessage(), request));
        } catch (RuntimeException exception) {
            JsonSupport.write(response, 500, error("INTERNAL_ERROR", "Unable to load courses", request));
        }
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            var body = JsonSupport.read(request, CreateCourseRequest.class);
            var course = services().courses().create(body.title(), body.description());
            JsonSupport.write(response, 201, CourseResponse.from(course));
        } catch (IllegalArgumentException exception) {
            JsonSupport.write(response, 400, error("INVALID_REQUEST", exception.getMessage(), request));
        } catch (IOException exception) {
            JsonSupport.write(response, 400, error("INVALID_JSON", "Request body must be valid JSON", request));
        } catch (RuntimeException exception) {
            JsonSupport.write(response, 500, error("INTERNAL_ERROR", "Unable to create course", request));
        }
    }
    private AppContextListener.Services services() { return (AppContextListener.Services) getServletContext().getAttribute(AppContextListener.SERVICES); }
    private static int parseInteger(String value, int fallback) { return value == null ? fallback : Integer.parseInt(value); }
    private static ErrorResponse error(String code, String message, HttpServletRequest request) { return new ErrorResponse(code, message, Instant.now(), String.valueOf(request.getAttribute("requestId"))); }
}
