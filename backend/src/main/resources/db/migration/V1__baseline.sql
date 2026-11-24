-- Milestone 1 baseline. Domain tables (users, events, registrations, attendance)
-- are added in later milestones. This table confirms Flyway can reach PostgreSQL.
CREATE TABLE IF NOT EXISTS app_meta (
    id SMALLINT PRIMARY KEY,
    initialized_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO app_meta (id)
VALUES (1)
ON CONFLICT (id) DO NOTHING;
