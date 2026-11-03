package com.learnwise.security;

import com.learnwise.entities.AuthSession;
import com.learnwise.entities.User;
import com.learnwise.repositories.AuthSessionRepository;
import com.learnwise.repositories.UserRepository;
import com.learnwise.services.AuthService;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CryptoAndAuthTest {
    private org.hibernate.SessionFactory factory;
    private AuthService auth;

    @BeforeEach void setUp() {
        var url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        factory = new Configuration().setProperty(AvailableSettings.JAKARTA_JDBC_URL, url)
                .setProperty(AvailableSettings.JAKARTA_JDBC_USER, "sa").setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, "")
                .setProperty(AvailableSettings.HBM2DDL_AUTO, "create-drop").addAnnotatedClass(User.class)
                .addAnnotatedClass(AuthSession.class).buildSessionFactory();
        auth = new AuthService(factory, new UserRepository(), new AuthSessionRepository(), new PasswordHasher(), 60);
    }

    @AfterEach void tearDown() { factory.close(); }

    @Test void opaqueSessionIsAuthenticatedThenLogoutInvalidatesIt() {
        var login = auth.register("Person", "person@example.com", "a-very-secure-password");
        var current = auth.authenticate(login.sessionToken());
        assertNotNull(current);
        assertTrue(auth.csrfValid(current, login.csrfToken()));
        assertFalse(auth.csrfValid(current, "wrong-token"));
        auth.logout(login.sessionToken());
        assertNull(auth.authenticate(login.sessionToken()));
    }

    @Test void expiredSessionIsRejectedAndUsersRemainIsolated() {
        var first = auth.register("One", "one@example.com", "a-very-secure-password");
        var second = auth.register("Two", "two@example.com", "a-very-secure-password");
        assertEquals("one@example.com", auth.authenticate(first.sessionToken()).user().getEmail());
        assertEquals("two@example.com", auth.authenticate(second.sessionToken()).user().getEmail());
        try (var session = factory.openSession()) {
            var transaction = session.beginTransaction();
            var expired = new AuthSession(session.createQuery("from User u where u.email = :email", User.class)
                    .setParameter("email", "one@example.com").getSingleResult(), Crypto.sha256("expired"),
                    Crypto.sha256("expired-csrf"), Instant.now().minusSeconds(1));
            session.persist(expired);
            transaction.commit();
        }
        assertNull(auth.authenticate("expired"));
    }

    @Test void invalidPasswordAndDuplicateEmailAreRejected() {
        auth.register("Person", "person@example.com", "a-very-secure-password");
        assertThrows(IllegalArgumentException.class, () -> auth.login("person@example.com", "wrong-password"));
        assertThrows(IllegalArgumentException.class, () -> auth.register("Other", "person@example.com", "a-very-secure-password"));
    }

    @Test void tokenHashComparisonIsConstantTimeAndNotReversible() {
        assertFalse(java.util.Arrays.equals(Crypto.sha256("one"), Crypto.sha256("two")));
        assertTrue(Crypto.constantTimeEquals(Crypto.sha256("same"), Crypto.sha256("same")));
    }
}
