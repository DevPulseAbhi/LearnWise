package com.learnwise.services;

import com.learnwise.entities.AuthSession;
import com.learnwise.entities.User;
import com.learnwise.repositories.AuthSessionRepository;
import com.learnwise.repositories.UserRepository;
import com.learnwise.security.Crypto;
import com.learnwise.security.PasswordHasher;
import org.hibernate.SessionFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class AuthService {
    public static final String SESSION_COOKIE = "LW_SESSION";
    public static final String CSRF_COOKIE = "LW_CSRF";
    private final SessionFactory sessionFactory;
    private final UserRepository users;
    private final AuthSessionRepository sessions;
    private final PasswordHasher passwords;
    private final int ttlMinutes;

    public AuthService(SessionFactory sessionFactory, UserRepository users, AuthSessionRepository sessions,
                       PasswordHasher passwords, int ttlMinutes) {
        this.sessionFactory = sessionFactory; this.users = users; this.sessions = sessions; this.passwords = passwords; this.ttlMinutes = ttlMinutes;
    }

    public LoginResult register(String name, String email, String password) {
        validateName(name); validateEmail(email); validatePassword(password);
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                var normalized = email.trim().toLowerCase();
                if (users.findByEmail(session, normalized).isPresent()) throw new IllegalArgumentException("An account with that email already exists");
                var user = new User(normalized, passwords.hash(password), name.trim());
                users.save(session, user);
                var result = createSession(session, user);
                transaction.commit();
                return result;
            } catch (RuntimeException exception) { if (transaction.isActive()) transaction.rollback(); throw exception; }
        }
    }

    public LoginResult login(String email, String password) {
        validateEmail(email);
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password is required");
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                var user = users.findByEmail(session, email.trim().toLowerCase()).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
                if (!passwords.matches(password, user.getPasswordHash())) throw new IllegalArgumentException("Invalid email or password");
                var result = createSession(session, user);
                transaction.commit();
                return result;
            } catch (RuntimeException exception) { if (transaction.isActive()) transaction.rollback(); throw exception; }
        }
    }

    private LoginResult createSession(org.hibernate.Session session, User user) {
        var sessionToken = Crypto.randomToken();
        var csrfToken = Crypto.randomToken();
        sessions.save(session, new AuthSession(user, Crypto.sha256(sessionToken), Crypto.sha256(csrfToken),
                Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES)));
        return new LoginResult(sessionToken, csrfToken, user);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 120) throw new IllegalArgumentException("Name must be between 1 and 120 characters");
    }
    private static void validateEmail(String email) {
        if (email == null || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.trim().length() > 254) throw new IllegalArgumentException("A valid email is required");
    }
    private static void validatePassword(String password) {
        if (password == null || password.length() < 12 || password.length() > 128) throw new IllegalArgumentException("Password must be between 12 and 128 characters");
    }

    public AuthenticatedSession authenticate(String token) {
        if (token == null || token.isBlank()) return null;
        try (var session = sessionFactory.openSession()) {
            var found = sessions.findActive(session, Crypto.sha256(token), Instant.now()).orElse(null);
            if (found == null) return null;
            return new AuthenticatedSession(found.getId(), found.getUser(), found.getCsrfTokenHash(), found.getExpiresAt());
        }
    }

    public boolean csrfValid(AuthenticatedSession authenticated, String csrfToken) {
        return authenticated != null && csrfToken != null && Crypto.constantTimeEquals(
                authenticated.csrfHash(), Crypto.sha256(csrfToken));
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) return;
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            var found = sessions.findActive(session, Crypto.sha256(token), Instant.now()).orElse(null);
            if (found != null) sessions.delete(session, found);
            transaction.commit();
        }
    }

    public User updateDisplayName(long userId, String displayName) {
            try (var session = sessionFactory.openSession()) {
                var transaction = session.beginTransaction();
                try {
                    var user = session.get(User.class, userId);
                    if (user == null) throw new IllegalArgumentException("User not found");
                    user.setDisplayName(displayName);
                    transaction.commit();
                    return user;
                } catch (RuntimeException exception) {
                    if (transaction.isActive()) transaction.rollback();
                    throw exception;
            }
        }
    }

    public record LoginResult(String sessionToken, String csrfToken, User user) {}
    public record AuthenticatedSession(Long id, User user, byte[] csrfHash, Instant expiresAt) {}
}
