CREATE TABLE subjects (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_subjects_slug (slug)
) ENGINE=InnoDB;

ALTER TABLE topics ADD COLUMN subject_id BIGINT UNSIGNED NULL;
ALTER TABLE topics ADD CONSTRAINT fk_topics_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL;

CREATE TABLE learning_content (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    topic_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(180) NOT NULL,
    body TEXT NOT NULL,
    content_type ENUM('LESSON', 'READING') NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_content_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE questions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    topic_id BIGINT UNSIGNED NOT NULL,
    prompt TEXT NOT NULL,
    question_type ENUM('SINGLE_CHOICE') NOT NULL,
    difficulty TINYINT UNSIGNED NOT NULL,
    explanation TEXT NULL,
    PRIMARY KEY (id),
    KEY ix_questions_topic_difficulty (topic_id, difficulty),
    CONSTRAINT ck_questions_difficulty CHECK (difficulty BETWEEN 1 AND 5),
    CONSTRAINT fk_questions_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE question_options (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    question_id BIGINT UNSIGNED NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT fk_options_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

ALTER TABLE quiz_attempts ADD COLUMN topic_id BIGINT UNSIGNED NULL;
ALTER TABLE quiz_attempts ADD COLUMN mode ENUM('DIAGNOSTIC', 'PRACTICE') NOT NULL DEFAULT 'PRACTICE';
ALTER TABLE quiz_attempts ADD COLUMN status ENUM('IN_PROGRESS', 'COMPLETED', 'ABANDONED') NOT NULL DEFAULT 'COMPLETED';
ALTER TABLE quiz_attempts ADD COLUMN started_at TIMESTAMP(6) NULL;
ALTER TABLE quiz_attempts ADD COLUMN completed_at TIMESTAMP(6) NULL;
ALTER TABLE quiz_attempts ADD COLUMN algorithm_version VARCHAR(20) NOT NULL DEFAULT 'mastery-v1';
ALTER TABLE quiz_attempts ADD KEY ix_attempts_user_topic (user_id, topic_id, attempted_at);
ALTER TABLE quiz_attempts ADD CONSTRAINT fk_attempts_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE RESTRICT;
UPDATE quiz_attempts SET started_at = attempted_at WHERE started_at IS NULL;

CREATE TABLE quiz_attempt_questions (
    attempt_id BIGINT UNSIGNED NOT NULL,
    question_id BIGINT UNSIGNED NOT NULL,
    question_order TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (attempt_id, question_id),
    UNIQUE KEY uk_attempt_question_order (attempt_id, question_order),
    CONSTRAINT fk_attempt_questions_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_questions_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE answer_records (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    attempt_id BIGINT UNSIGNED NOT NULL,
    question_id BIGINT UNSIGNED NOT NULL,
    selected_option_id BIGINT UNSIGNED NULL,
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_answer_attempt_question (attempt_id, question_id),
    CONSTRAINT fk_answers_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_answers_option FOREIGN KEY (selected_option_id) REFERENCES question_options(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE topic_mastery (
    user_id BIGINT UNSIGNED NOT NULL,
    topic_id BIGINT UNSIGNED NOT NULL,
    mastery_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
    sample_size INT UNSIGNED NOT NULL DEFAULT 0,
    recent_accuracy DECIMAL(5,2) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    algorithm_version VARCHAR(20) NOT NULL DEFAULT 'mastery-v1',
    PRIMARY KEY (user_id, topic_id),
    CONSTRAINT fk_mastery_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_mastery_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE learning_history (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    topic_id BIGINT UNSIGNED NOT NULL,
    activity_type ENUM('QUIZ_STARTED', 'QUIZ_COMPLETED', 'LESSON_VIEWED') NOT NULL,
    reference_id BIGINT UNSIGNED NULL,
    occurred_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_history_user_time (user_id, occurred_at),
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO subjects (name, slug) VALUES ('Design', 'design'), ('Computer science', 'computer-science');
INSERT INTO topics (name, slug, subject_id) VALUES
    ('Research methods', 'research-methods', (SELECT id FROM subjects WHERE slug = 'design')),
    ('Python foundations', 'python-foundations', (SELECT id FROM subjects WHERE slug = 'computer-science'));
INSERT INTO questions (topic_id, prompt, question_type, difficulty, explanation) VALUES
    ((SELECT id FROM topics WHERE slug = 'research-methods'), 'Which question is most open-ended?', 'SINGLE_CHOICE', 1, 'Open-ended questions invite observation and detail rather than a yes or no response.'),
    ((SELECT id FROM topics WHERE slug = 'research-methods'), 'What should a researcher do before writing interview questions?', 'SINGLE_CHOICE', 2, 'Clarifying the learning goal prevents questions from becoming disconnected from the research purpose.'),
    ((SELECT id FROM topics WHERE slug = 'python-foundations'), 'Which value is a Python list?', 'SINGLE_CHOICE', 1, 'Square brackets create a list in Python.'),
    ((SELECT id FROM topics WHERE slug = 'python-foundations'), 'What does len(items) return?', 'SINGLE_CHOICE', 2, 'len returns the number of items in a sequence.');
INSERT INTO question_options (question_id, option_text, is_correct) VALUES
    (1, 'What did you notice during the workshop?', TRUE), (1, 'Did you enjoy the workshop?', FALSE), (1, 'Was it useful?', FALSE),
    (2, 'Clarify the learning goal', TRUE), (2, 'Choose the longest questions', FALSE), (2, 'Avoid all follow-ups', FALSE),
    (3, '[1, 2, 3]', TRUE), (3, '(1, 2, 3)', FALSE), (3, '{1, 2, 3}', FALSE),
    (4, 'The number of items', TRUE), (4, 'The last item', FALSE), (4, 'A sorted copy', FALSE);
