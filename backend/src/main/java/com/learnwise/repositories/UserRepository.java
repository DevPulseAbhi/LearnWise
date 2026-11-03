package com.learnwise.repositories;

import com.learnwise.entities.User;
import org.hibernate.Session;
import java.util.Optional;

public final class UserRepository {
    public Optional<User> findByEmail(Session session, String email) {
        return session.createQuery("from User u where lower(u.email) = :email", User.class)
                .setParameter("email", email.toLowerCase()).uniqueResultOptional();
    }
    public void save(Session session, User user) { session.persist(user); }
}
