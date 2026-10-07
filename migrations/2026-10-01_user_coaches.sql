-- Moves coach <-> athlete links from the two old tables (users_coaches,
-- users_coaching_athletes) into the single user_coaches table used by UserEntity.
-- Safe to run more than once, before or after deploying the new server.
-- Check the old column names first:  \d users_coaches   and   \d users_coaching_athletes
BEGIN;

CREATE TABLE IF NOT EXISTS user_coaches (
    athlete_id uuid NOT NULL REFERENCES users(id),
    coach_id   uuid NOT NULL REFERENCES users(id),
    PRIMARY KEY (athlete_id, coach_id)
);

INSERT INTO user_coaches (athlete_id, coach_id)
SELECT user_entity_id, coaches_id
FROM users_coaches
WHERE user_entity_id IS NOT NULL AND coaches_id IS NOT NULL
UNION
SELECT coaching_athletes_id, user_entity_id
FROM users_coaching_athletes
WHERE user_entity_id IS NOT NULL AND coaching_athletes_id IS NOT NULL
ON CONFLICT DO NOTHING;

COMMIT;

-- Once everything works, the old tables can go:
-- DROP TABLE users_coaches;
-- DROP TABLE users_coaching_athletes;
