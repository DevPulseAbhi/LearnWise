# LearnWise backend foundation

The backend is a plain Jakarta Servlet WAR for Tomcat 11. It does not use Spring Boot.

## Prerequisites

- Java 21 (the local verification machine must use the requested runtime; the current machine exposes Java 26)
- Maven 3.9+
- MySQL 8.0+
- Tomcat 11

Create a database and least-privilege application user, then export the variables in [`.env.example`](./.env.example) in the process environment. Never commit a real `.env`. The local MySQL URL includes `allowPublicKeyRetrieval=true` because MySQL 8+ commonly uses `caching_sha2_password`; use TLS and remove `useSSL=false` for production.

```sql
CREATE DATABASE learnwise CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'learnwise'@'localhost' IDENTIFIED BY 'change-me';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON learnwise.* TO 'learnwise'@'localhost';
```

Run `mvn clean verify` and deploy `target/learnwise.war` to Tomcat 11. The WAR is expected to be available at `http://localhost:8080/learnwise`. Flyway applies `src/main/resources/db/migration/V1__foundation.sql` on startup when `LEARNWISE_DB_MIGRATE=true`. Hibernate uses `validate`, never destructive automatic DDL.

For local development, export all variables from [`.env.example`](./.env.example) into the Tomcat process before starting it; Tomcat does not automatically load `.env` files. Verify the backend before opening the frontend:

```text
GET http://localhost:8080/learnwise/api/v1/health
```

The frontend defaults to `http://localhost:8080/learnwise/api/v1`. To use another deployment context or port, create a root `.env` from [`../.env.example`](../.env.example), set `VITE_API_BASE_URL`, and restart Vite.

## Responsibilities

- **Hibernate:** entities, normal CRUD, relationships, and unit-of-work persistence.
- **JDBC:** health connectivity check and explicitly scoped analytics/bulk SQL only. `JdbcHealthRepository` uses a prepared statement and a try-with-resources connection.
- A service owns transaction boundaries. Hibernate writes are committed or rolled back as one unit; JDBC health checks are read-only and do not share write transactions.

## Foundation endpoints

- `GET /api/v1/health` — application and database status; database errors return a generic `503` response.
- `GET /api/v1/courses?page=0&size=20&topic=...` — paginated course foundation data.
- `POST /api/v1/courses` — validates and persists a course.

Other learning features remain intentionally unimplemented until their approved API contracts are available. Unknown routes return a structured `404`.

## Learning engine and quiz flow

Phase 4 supports authoritative `SINGLE_CHOICE` questions only. The seed content in `V3__learning_engine.sql` is clearly product development content, not user-generated or AI-generated content. Unsupported question types are rejected.

The flow is:

1. `POST /api/v1/quizzes/start` with `{ "topicId": 1, "mode": "PRACTICE" }`.
2. The server selects up to five questions from that topic and returns prompts/options without correctness.
3. `POST /api/v1/quizzes/{attemptId}/answers` with `{ "questionId": 1, "optionId": 2 }`.
4. The server validates ownership, attempt membership, option membership, and duplicate answers, then calculates correctness from the database.
5. On the final answer the attempt is completed, score and answer history are persisted, and topic mastery is updated.
6. `GET /api/v1/quizzes/history?topicId=1` returns only the authenticated user's attempts.

Mastery algorithm `mastery-v1` is deterministic and explainable:

- Fewer than three prior answers: mastery is provisional and questions target difficulty 1.
- Otherwise, mastery is the running mean of completed-attempt accuracy, capped at 100.
- Difficulty target is 1 below 60% mastery, 2 from 60–79%, and 3 at 80% or above.
- Recent accuracy is stored with each mastery record; the first implementation intentionally does not claim scientific validation.
- Topics with no attempts use the lowest difficulty and cannot be treated as mastered.
- A completed attempt updates mastery using the whole attempt's accuracy, not a client-provided score.

Repeated answer submissions are rejected, abandoned/in-progress attempts remain resumable by the owning user, and attempts are isolated by authenticated user ID. No frontend user ID or score is trusted.

## Email/password authentication

`POST /api/v1/auth/register` accepts `name`, `email`, `password`, and `confirmPassword`. `POST /api/v1/auth/login` accepts `email` and `password`. Passwords must be 12–128 characters and are hashed with BCrypt work factor 12; plaintext passwords are never persisted. Email uniqueness is enforced by the database and checked in the service.

LearnWise creates an opaque random session token and CSRF token. Only SHA-256 hashes are persisted in `auth_sessions`; the browser receives an HttpOnly session cookie and a readable CSRF cookie. State-changing requests must send `X-CSRF-Token`. Cookies use `SameSite=Lax`; production must set `LEARNWISE_COOKIE_SECURE=true`.

Protected profile and learning endpoints derive the local user from the verified server-side session. No frontend-supplied user ID is used for authorization. Logout invalidates the server-side session.

## Phase 5 documents and tutor

Authenticated PDF endpoints:

- `POST /api/v1/documents` — multipart field `file`; validates the `.pdf` extension, `application/pdf` MIME type, PDF magic signature, and configurable size limit.
- `GET /api/v1/documents` — lists only the authenticated user's documents.
- `POST /api/v1/documents/{id}/retry` — retries only an owned failed document.
- `DELETE /api/v1/documents/{id}` — deletes only an owned document and its stored file.
- `POST /api/v1/tutor` — `{ "message": "...", "documentId": 1 }`; retrieves owned document chunks and returns page citations.

PDF files are stored outside the web root under `LEARNWISE_DOCUMENT_STORAGE_PATH`, using generated UUID keys. Original filenames are metadata only and never become paths. No OCR or embedded-content execution is performed. Processing errors expose a safe error code, not parser details. The default tutor provider is `mock`; it returns grounded excerpts or explicitly acknowledges insufficient evidence. No provider API key is required for tests, and no live provider call is claimed.

The local filesystem adapter is suitable for development. Production should replace it with an object-storage adapter and configure a server-side provider adapter, timeout, retry, rate-limit, and per-user budget policy before enabling live AI.

## Tests

`mvn test` uses H2 in MySQL compatibility mode for repository/transaction tests. A real MySQL integration test is opt-in via `-Pintegration` and `LEARNWISE_IT_DB_URL`, `LEARNWISE_IT_DB_USER`, and `LEARNWISE_IT_DB_PASSWORD`; it is not silently replaced by an in-memory database.
