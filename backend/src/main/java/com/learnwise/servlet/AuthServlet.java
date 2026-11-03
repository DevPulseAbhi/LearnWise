package com.learnwise.servlet;

import com.learnwise.configuration.AuthConfig;
import com.learnwise.dto.*;
import com.learnwise.services.AuthService;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public final class AuthServlet extends HttpServlet {
    private AuthConfig config;
    private AuthService auth;

    @Override public void init() {
        config = (AuthConfig) getServletContext().getAttribute(AuthConfig.class.getName());
        auth = (AuthService) getServletContext().getAttribute(AuthService.class.getName());
    }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var path = request.getPathInfo() == null ? "" : request.getPathInfo();
        if ("/me".equals(path)) {
            var current = current(request);
            if (current == null) { writeError(response, 401, "UNAUTHENTICATED", "Sign in is required", request); return; }
            JsonSupport.write(response, 200, UserResponse.from(current.user()));
        } else if ("/csrf".equals(path)) {
            var current = current(request);
            if (current == null) { writeError(response, 401, "UNAUTHENTICATED", "Sign in is required", request); return; }
            JsonSupport.write(response, 200, Map.of("csrfToken", AuthCookies.value(request, AuthService.CSRF_COOKIE)));
        } else writeError(response, 404, "NOT_FOUND", "Authentication route not found", request);
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var path = request.getPathInfo() == null ? "" : request.getPathInfo();
        try {
            if ("/register".equals(path)) {
                var body = JsonSupport.read(request, RegisterRequest.class);
                if (body.confirmPassword() == null || !body.password().equals(body.confirmPassword())) throw new IllegalArgumentException("Passwords do not match");
                writeLogin(response, auth.register(body.name(), body.email(), body.password()));
            } else if ("/login".equals(path)) {
                var body = JsonSupport.read(request, LoginRequest.class);
                writeLogin(response, auth.login(body.email(), body.password()));
            } else if ("/logout".equals(path)) {
                auth.logout(AuthCookies.value(request, AuthService.SESSION_COOKIE));
                AuthCookies.clear(response, AuthService.SESSION_COOKIE, config.secureCookie());
                AuthCookies.clear(response, AuthService.CSRF_COOKIE, config.secureCookie());
                response.setStatus(204);
            } else writeError(response, 404, "NOT_FOUND", "Authentication route not found", request);
        } catch (IllegalArgumentException exception) {
            writeError(response, 400, "INVALID_REQUEST", exception.getMessage(), request);
        } catch (IOException exception) {
            writeError(response, 400, "INVALID_JSON", "Request body must be valid JSON", request);
        }
    }

    private void writeLogin(HttpServletResponse response, AuthService.LoginResult result) throws IOException {
        AuthCookies.set(response, AuthService.SESSION_COOKIE, result.sessionToken(), config.sessionTtlMinutes() * 60, config.secureCookie(), true);
        AuthCookies.set(response, AuthService.CSRF_COOKIE, result.csrfToken(), config.sessionTtlMinutes() * 60, config.secureCookie(), false);
        JsonSupport.write(response, 200, UserResponse.from(result.user()));
    }
    private static AuthService.AuthenticatedSession current(HttpServletRequest request) {
        return (AuthService.AuthenticatedSession) request.getAttribute(AuthFilter.AUTHENTICATED);
    }
    private static void writeError(HttpServletResponse response, int status, String code, String message, HttpServletRequest request) throws IOException {
        JsonSupport.write(response, status, new ErrorResponse(code, message, Instant.now(), String.valueOf(request.getAttribute("requestId"))));
    }
}
