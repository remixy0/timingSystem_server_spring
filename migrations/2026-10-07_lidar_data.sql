-- Adds the lidar_data table: the raw LiDAR capture of an effort (one row per effort).
-- Hibernate creates it automatically when ddl-auto=update; run this if production uses validate/none.
-- Safe to run more than once.
CREATE TABLE IF NOT EXISTS lidar_data (
    effort_id      uuid PRIMARY KEY,
    owner_id       varchar(255),
    format_version integer NOT NULL DEFAULT 1,
    device_name    varchar(255),
    started_at     varchar(255),
    time_ms        integer[],
    distance_cm    integer[],
    strength       integer[],
    lost_packets   integer NOT NULL DEFAULT 0,
    ended_by_lidar boolean NOT NULL DEFAULT false,
    timer_offset   double precision NOT NULL DEFAULT 0,
    saw_start      boolean NOT NULL DEFAULT true
);

-- Optional (Hibernate does not create it): makes "which of my efforts have LiDAR data" fast
CREATE INDEX IF NOT EXISTS idx_lidar_data_owner_id ON lidar_data (owner_id);
