package com.learnwise.servlet;

import com.learnwise.services.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;

public final class AuthFilter implements Filter {
    public static final String AUTHENTICATED = AuthFilter.class.getName() + ".authenticated";
    private AuthService auth;
    @Override public void init(FilterConfig filterConfig) { auth = (AuthService) filterConfig.getServletContext().getAttribute(AuthService.class.getName()); }
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;
        var path = httpRequest.getRequestURI();
        var current = auth.authenticate(AuthCookies.value(httpRequest, AuthService.SESSION_COOKIE));
        if (current != null) request.setAttribute(AUTHENTICATED, current);
        var protectedRoute = path.endsWith("/auth/me") || path.endsWith("/auth/csrf") || path.endsWith("/profile") || path.contains("/quizzes") || path.contains("/documents") || path.contains("/tutor") || ("POST".equals(httpRequest.getMethod()) && path.endsWith("/courses"));
        if (protectedRoute && current == null) {
            JsonSupport.write((HttpServletResponse) response, 401, new com.learnwise.dto.ErrorResponse("UNAUTHENTICATED", "Sign in is required", Instant.now(), String.valueOf(request.getAttribute("requestId"))));
            return;
        }
        var mutating = switch (httpRequest.getMethod()) { case "POST", "PUT", "PATCH", "DELETE" -> true; default -> false; };
        var publicAuthRequest = path.endsWith("/auth/register") || path.endsWith("/auth/login");
        if (mutating && !publicAuthRequest && !path.endsWith("/auth/logout")) {
            var csrf = httpRequest.getHeader("X-CSRF-Token");
            if (current == null || !auth.csrfValid(current, csrf)) {
                JsonSupport.write((HttpServletResponse) response, 403, new com.learnwise.dto.ErrorResponse("CSRF_INVALID", "Request could not be verified", Instant.now(), String.valueOf(request.getAttribute("requestId"))));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
