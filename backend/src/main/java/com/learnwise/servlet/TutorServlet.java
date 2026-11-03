package com.learnwise.servlet;

import com.learnwise.services.*;
import com.learnwise.dto.ErrorResponse;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;

public final class TutorServlet extends HttpServlet {
    private AiTutorService tutor;
    @Override public void init() { tutor = (AiTutorService) getServletContext().getAttribute(AiTutorService.class.getName()); }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var current = (AuthService.AuthenticatedSession) request.getAttribute(AuthFilter.AUTHENTICATED);
        try {
            var body = JsonSupport.read(request, TutorRequest.class);
            JsonSupport.write(response, 200, tutor.answer(current.user().getId(), body.message(), body.documentId()));
        } catch (IllegalArgumentException exception) {
            JsonSupport.write(response, 400, new ErrorResponse("INVALID_TUTOR_REQUEST", exception.getMessage(), Instant.now(), String.valueOf(request.getAttribute("requestId"))));
        } catch (IllegalStateException exception) {
            JsonSupport.write(response, 429, new ErrorResponse("TUTOR_RATE_LIMIT", exception.getMessage(), Instant.now(), String.valueOf(request.getAttribute("requestId"))));
        }
    }
    public record TutorRequest(String message, Long documentId) {}
}
