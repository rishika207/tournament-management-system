-- ============================================================
-- Multi-Sport Tournament Management System - MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS tournament_db;
USE tournament_db;

-- ─────────────────────────────────────────
-- USERS (Base + Role-based actors)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) UNIQUE NOT NULL,
    role        ENUM('Admin','Organizer','TeamManager','Referee','Viewer') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- LOCATIONS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS locations (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    venue_name  VARCHAR(150) NOT NULL,
    city        VARCHAR(100) NOT NULL
);

-- ─────────────────────────────────────────
-- TOURNAMENTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tournaments (
    tournament_id   INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    sport_type      ENUM('Cricket','Football','Badminton') NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    location_id     INT,
    created_by      INT NOT NULL,
    status          ENUM('Upcoming','Ongoing','Completed') DEFAULT 'Upcoming',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (location_id)  REFERENCES locations(location_id),
    FOREIGN KEY (created_by)   REFERENCES users(user_id)
);

-- ─────────────────────────────────────────
-- TEAMS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS teams (
    team_id         INT AUTO_INCREMENT PRIMARY KEY,
    team_name       VARCHAR(150) NOT NULL,
    manager_id      INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(user_id)
);

-- ─────────────────────────────────────────
-- PLAYERS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS players (
    player_id   INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    age         INT NOT NULL,
    skill       ENUM('Beginner','Intermediate','Advanced','Pro') DEFAULT 'Beginner',
    team_id     INT NOT NULL,
    FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────
-- REGISTRATIONS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    tournament_id   INT NOT NULL,
    team_id         INT NOT NULL,
    status          ENUM('Pending','Confirmed','Rejected') DEFAULT 'Pending',
    registered_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id),
    FOREIGN KEY (team_id)       REFERENCES teams(team_id),
    UNIQUE KEY uq_reg (tournament_id, team_id)
);

-- ─────────────────────────────────────────
-- PAYMENTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payments (
    payment_id      INT AUTO_INCREMENT PRIMARY KEY,
    registration_id INT NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    status          ENUM('Pending','Completed','Failed') DEFAULT 'Pending',
    gateway_ref     VARCHAR(100),
    paid_at         TIMESTAMP NULL,
    FOREIGN KEY (registration_id) REFERENCES registrations(registration_id)
);

-- ─────────────────────────────────────────
-- MATCHES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS matches (
    match_id        INT AUTO_INCREMENT PRIMARY KEY,
    tournament_id   INT NOT NULL,
    team1_id        INT NOT NULL,
    team2_id        INT NOT NULL,
    referee_id      INT,
    location_id     INT,
    match_date      DATETIME NOT NULL,
    status          ENUM('Scheduled','InProgress','Completed','Cancelled') DEFAULT 'Scheduled',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id),
    FOREIGN KEY (team1_id)      REFERENCES teams(team_id),
    FOREIGN KEY (team2_id)      REFERENCES teams(team_id),
    FOREIGN KEY (referee_id)    REFERENCES users(user_id),
    FOREIGN KEY (location_id)   REFERENCES locations(location_id)
);

-- ─────────────────────────────────────────
-- SCORES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS scores (
    score_id        INT AUTO_INCREMENT PRIMARY KEY,
    match_id        INT NOT NULL UNIQUE,
    team1_score     VARCHAR(50) NOT NULL DEFAULT '0',
    team2_score     VARCHAR(50) NOT NULL DEFAULT '0',
    winner_team_id  INT,
    updated_by      INT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id)       REFERENCES matches(match_id),
    FOREIGN KEY (winner_team_id) REFERENCES teams(team_id),
    FOREIGN KEY (updated_by)     REFERENCES users(user_id)
);

-- ─────────────────────────────────────────
-- STANDINGS VIEW
-- ─────────────────────────────────────────
CREATE OR REPLACE VIEW standings AS
SELECT
    t.tournament_id,
    t.name AS tournament_name,
    tm.team_id,
    tm.team_name,
    COUNT(CASE WHEN s.winner_team_id = tm.team_id THEN 1 END) AS wins,
    COUNT(CASE WHEN m.status = 'Completed' AND s.winner_team_id != tm.team_id AND s.winner_team_id IS NOT NULL THEN 1 END) AS losses,
    COUNT(CASE WHEN m.status = 'Completed' AND s.winner_team_id IS NULL THEN 1 END) AS draws,
    COUNT(CASE WHEN m.status = 'Completed' THEN 1 END) AS played
FROM tournaments t
JOIN registrations r  ON r.tournament_id = t.tournament_id AND r.status = 'Confirmed'
JOIN teams tm          ON tm.team_id = r.team_id
LEFT JOIN matches m    ON m.tournament_id = t.tournament_id
                      AND (m.team1_id = tm.team_id OR m.team2_id = tm.team_id)
                      AND m.status = 'Completed'
LEFT JOIN scores s     ON s.match_id = m.match_id
GROUP BY t.tournament_id, t.name, tm.team_id, tm.team_name
ORDER BY t.tournament_id, wins DESC;

-- ─────────────────────────────────────────
-- SEED DATA
-- ─────────────────────────────────────────
INSERT IGNORE INTO users (name, email, role, password_hash) VALUES
('Super Admin',    'admin@tournament.com',      'Admin',        SHA2('admin123',256)),
('Alice Organizer','alice@tournament.com',      'Organizer',    SHA2('alice123',256)),
('Bob Manager',    'bob@tournament.com',        'TeamManager',  SHA2('bob123',256)),
('Carol Referee',  'carol@tournament.com',      'Referee',      SHA2('carol123',256)),
('Dave Viewer',    'dave@tournament.com',       'Viewer',       SHA2('dave123',256)),
('Eve Manager',    'eve@tournament.com',        'TeamManager',  SHA2('eve123',256));

INSERT IGNORE INTO locations (venue_name, city) VALUES
('Eden Gardens',      'Kolkata'),
('Wankhede Stadium',  'Mumbai'),
('Jawaharlal Nehru Stadium', 'Delhi'),
('Chinnaswamy Stadium','Bengaluru'),
('Sports Complex A',  'Chennai');

INSERT IGNORE INTO teams (team_name, manager_id) VALUES
('Thunder Hawks',  3),
('Storm Riders',   6),
('Iron Giants',    3),
('Blue Falcons',   6);

INSERT IGNORE INTO players (name, age, skill, team_id) VALUES
('Rahul Sharma', 24, 'Pro', 1),
('Vikas Rao',    22, 'Advanced', 1),
('Amit Singh',   26, 'Intermediate', 2),
('Priya Das',    21, 'Pro', 2),
('Kiran Nair',   25, 'Advanced', 3),
('Sunita Patel', 23, 'Beginner', 4);
