package com.learnwise.servlet;

import jakarta.servlet.http.*;

public final class AuthCookies {
    private AuthCookies() {}
    public static String value(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) if (name.equals(cookie.getName())) return cookie.getValue();
        return null;
    }
    public static void set(HttpServletResponse response, String name, String value, int maxAge, boolean secure, boolean httpOnly) {
        response.addHeader("Set-Cookie", name + "=" + value + "; Max-Age=" + maxAge + "; Path=/; SameSite=Lax"
                + (secure ? "; Secure" : "") + (httpOnly ? "; HttpOnly" : ""));
    }
    public static void clear(HttpServletResponse response, String name, boolean secure) { set(response, name, "", 0, secure, true); }
}
