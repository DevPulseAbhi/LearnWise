package com.learnwise.servlet;

import com.learnwise.configuration.AuthConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class CorsFilter implements Filter {
    private AuthConfig config;
    @Override public void init(FilterConfig filterConfig) { config = (AuthConfig) filterConfig.getServletContext().getAttribute(AuthConfig.class.getName()); }
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var http = (HttpServletResponse) response;
        http.setHeader("Access-Control-Allow-Origin", config.frontendOrigin());
        http.setHeader("Vary", "Origin");
        http.setHeader("Access-Control-Allow-Credentials", "true");
        http.setHeader("Access-Control-Allow-Headers", "Content-Type, X-CSRF-Token");
        http.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
        if ("OPTIONS".equals(((jakarta.servlet.http.HttpServletRequest) request).getMethod())) { http.setStatus(204); return; }
        chain.doFilter(request, response);
    }
}
