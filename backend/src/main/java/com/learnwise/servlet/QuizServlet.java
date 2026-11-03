package com.learnwise.servlet;

import com.learnwise.dto.*;
import com.learnwise.entities.QuizMode;
import com.learnwise.services.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;

public final class QuizServlet extends HttpServlet {
    private QuizService quizzes;
    @Override public void init() { quizzes = (QuizService) getServletContext().getAttribute(QuizService.class.getName()); }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var current = current(request);
        try {
            var path = request.getPathInfo() == null ? "" : request.getPathInfo();
            if ("/start".equals(path)) {
                var body = JsonSupport.read(request, StartQuizRequest.class);
                var mode = "DIAGNOSTIC".equalsIgnoreCase(body.mode()) ? QuizMode.DIAGNOSTIC : QuizMode.PRACTICE;
                JsonSupport.write(response, 201, quizzes.start(current.user().getId(), body.topicId(), mode));
                return;
            }
            if (path.matches("/\\d+/answers")) {
                var attemptId = Long.parseLong(path.substring(1, path.indexOf("/answers")));
                var body = JsonSupport.read(request, AnswerRequest.class);
                JsonSupport.write(response, 200, quizzes.answer(current.user().getId(), attemptId, body.questionId(), body.optionId()));
                return;
            }
            JsonSupport.write(response, 404, error("NOT_FOUND", "Quiz route not found", request));
        } catch (IllegalArgumentException exception) {
            JsonSupport.write(response, 400, error("INVALID_REQUEST", exception.getMessage(), request));
        } catch (IllegalStateException exception) {
            JsonSupport.write(response, 409, error("INVALID_ATTEMPT_STATE", exception.getMessage(), request));
        } catch (IOException exception) {
            JsonSupport.write(response, 400, error("INVALID_JSON", "Request body must be valid JSON", request));
        } catch (RuntimeException exception) {
            JsonSupport.write(response, 500, error("QUIZ_ERROR", "Unable to process quiz request", request));
        }
    }
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var current = current(request);
        try {
            var topicId = Long.parseLong(request.getParameter("topicId"));
            JsonSupport.write(response, 200, quizzes.history(current.user().getId(), topicId));
        } catch (NumberFormatException exception) {
            JsonSupport.write(response, 400, error("INVALID_REQUEST", "topicId is required", request));
        }
    }
    private static AuthService.AuthenticatedSession current(HttpServletRequest request) { return (AuthService.AuthenticatedSession) request.getAttribute(AuthFilter.AUTHENTICATED); }
    private static ErrorResponse error(String code, String message, HttpServletRequest request) { return new ErrorResponse(code, message, Instant.now(), String.valueOf(request.getAttribute("requestId"))); }
}
