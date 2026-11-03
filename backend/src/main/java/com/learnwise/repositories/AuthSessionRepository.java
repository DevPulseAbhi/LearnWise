package com.learnwise.repositories;

import com.learnwise.entities.AuthSession;
import org.hibernate.Session;
import java.time.Instant;
import java.util.Optional;

public final class AuthSessionRepository {
    public Optional<AuthSession> findActive(Session session, byte[] tokenHash, Instant now) {
        return session.createQuery("from AuthSession s join fetch s.user where s.tokenHash = :tokenHash and s.expiresAt > :now", AuthSession.class)
                .setParameter("tokenHash", tokenHash).setParameter("now", now).uniqueResultOptional();
    }
    public void save(Session session, AuthSession authSession) { session.persist(authSession); }
    public void delete(Session session, AuthSession authSession) { session.remove(authSession); }
}
