CREATE TABLE study_plans (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(180) NOT NULL,
    goal VARCHAR(500) NOT NULL,
    deadline DATE NULL,
    daily_minutes SMALLINT UNSIGNED NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), KEY ix_plans_user_updated (user_id, updated_at),
    CONSTRAINT fk_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE study_tasks (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    plan_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    topic_id BIGINT UNSIGNED NOT NULL,
    task_type ENUM('PRACTICE','REVIEW','REVISION') NOT NULL,
    title VARCHAR(180) NOT NULL,
    due_at TIMESTAMP(6) NOT NULL,
    status ENUM('OPEN','COMPLETED','POSTPONED') NOT NULL DEFAULT 'OPEN',
    completed_at TIMESTAMP(6) NULL,
    rescheduled_from TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), KEY ix_tasks_user_due (user_id, due_at, status),
    CONSTRAINT fk_tasks_plan FOREIGN KEY (plan_id) REFERENCES study_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE topic_prerequisites (
    topic_id BIGINT UNSIGNED NOT NULL,
    prerequisite_topic_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (topic_id, prerequisite_topic_id),
    CONSTRAINT fk_prereq_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    CONSTRAINT fk_prereq_parent FOREIGN KEY (prerequisite_topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    CONSTRAINT ck_no_self_prereq CHECK (topic_id <> prerequisite_topic_id)
) ENGINE=InnoDB;
