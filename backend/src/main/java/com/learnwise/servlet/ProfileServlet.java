package com.learnwise.servlet;

import com.learnwise.dto.*;
import com.learnwise.services.AuthService;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;

public final class ProfileServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var current = current(request);
        JsonSupport.write(response, 200, UserResponse.from(current.user()));
    }
    @Override protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            var body = JsonSupport.read(request, UpdateProfileRequest.class);
            if (body.displayName() == null || body.displayName().isBlank() || body.displayName().length() > 120) {
                JsonSupport.write(response, 400, error("INVALID_REQUEST", "displayName must be between 1 and 120 characters", request)); return;
            }
            var current = current(request);
            var user = ((AuthService) getServletContext().getAttribute(AuthService.class.getName()))
                    .updateDisplayName(current.user().getId(), body.displayName().trim());
            JsonSupport.write(response, 200, UserResponse.from(user));
        } catch (IOException exception) {
            JsonSupport.write(response, 400, error("INVALID_JSON", "Request body must be valid JSON", request));
        }
    }
    private static AuthService.AuthenticatedSession current(HttpServletRequest request) {
        return (AuthService.AuthenticatedSession) request.getAttribute(AuthFilter.AUTHENTICATED);
    }
    private static ErrorResponse error(String code, String message, HttpServletRequest request) {
        return new ErrorResponse(code, message, Instant.now(), String.valueOf(request.getAttribute("requestId")));
    }
}
