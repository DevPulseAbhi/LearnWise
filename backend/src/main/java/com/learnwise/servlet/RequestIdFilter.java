package com.learnwise.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

public final class RequestIdFilter implements Filter {
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;
        var requestId = UUID.randomUUID().toString();
        httpResponse.setHeader("X-Request-Id", requestId);
        httpRequest.setAttribute("requestId", requestId);
        chain.doFilter(request, response);
    }
}
