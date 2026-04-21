# 🏆 Multi-Sport Tournament Management System

A full-stack tournament platform built with **Java (OOP + Design Patterns)**, **MySQL**, and **Streamlit (Python)**.

---

## 📁 Project Structure

```
tournament-system/
├── mysql/
│   └── schema.sql                  ← Database schema + seed data
│
├── java-backend/
│   ├── pom.xml                     ← Maven build (Spark Java + Gson + MySQL JDBC)
│   └── src/main/java/com/tournament/
│       ├── model/
│       │   ├── User.java           ← Abstract base class
│       │   ├── Actors.java         ← Admin, Organizer, TeamManager, Referee, Viewer
│       │   └── DomainModels.java   ← Sport(interface), Cricket/Football/Badminton,
│       │                             Tournament, Team, Player, Match, Score,
│       │                             Registration, Payment, Location
│       ├── factory/
│       │   └── SportFactory.java   ← Factory Pattern: createSport(type)
│       ├── command/
│       │   └── Commands.java       ← Command Pattern: all commands + SystemController
│       └── api/
│           └── TournamentApiServer.java  ← REST API (Spark Java, port 4567)
│
└── streamlit-frontend/
    ├── requirements.txt
    └── app.py                      ← Full Streamlit UI (6 pages)
```

---

## 🏗️ Architecture

```
┌─────────────────────┐     HTTP/JSON      ┌──────────────────────┐
│  Streamlit Frontend │ ──────────────────► │  Java REST API       │
│  (Python, port 8501)│ ◄────────────────── │  (Spark, port 4567)  │
└─────────────────────┘                     └──────────┬───────────┘
                                                        │ JDBC
                                                        ▼
                                            ┌──────────────────────┐
                                            │     MySQL Database    │
                                            │    tournament_db      │
                                            └──────────────────────┘
```

---

## 🎨 Design Patterns

### Factory Pattern — `SportFactory`
```java
// GOOD: use the factory
Sport s = SportFactory.createSport("Cricket");   // returns Cricket instance
Sport f = SportFactory.createSport("Football");  // returns Football instance

// BAD: never instantiate directly
Cricket c = new Cricket();  // ← don't do this outside of factory
```

The factory validates the sport type (also used in the REST API when creating tournaments) and returns the correct polymorphic `Sport` implementation.

### Command Pattern

```
Command Interface       Concrete Commands            Invoker              Receiver
─────────────────      ──────────────────────       ────────────────     ─────────────────
execute()          ─►  RegisterTeamCommand     ─►   SystemController ─►  TournamentSystem
                        ScheduleMatchCommand         .addCommand()        .registerTeam()
                        UpdateScoreCommand            .executeAll()        .scheduleMatch()
                        MakePaymentCommand            .executeNow()        .updateScore()
                                                                           .makePayment()
```

Usage:
```java
TournamentSystem  ts  = new TournamentSystem();
SystemController  sc  = new SystemController();

sc.addCommand(new RegisterTeamCommand(ts, 1, 3));
sc.addCommand(new MakePaymentCommand(ts, 1, 500.0));
sc.executeAll();  // executes both in order
```

---

## 👥 Actors & Responsibilities

| Actor           | Role                                             | Pattern Used            |
|-----------------|--------------------------------------------------|-------------------------|
| Admin           | Creates tournaments                              | Factory (validates sport)|
| Organizer       | Schedules matches, assigns referees              | ScheduleMatchCommand    |
| TeamManager     | Registers teams, adds players                   | RegisterTeamCommand     |
| Referee         | Updates scores, declares results                | UpdateScoreCommand      |
| Viewer          | Reads schedules, results, standings             | —                       |
| Payment Gateway | External: processes registration payments       | MakePaymentCommand      |

---

## 🚀 Setup & Run

### Step 1: MySQL

```bash
mysql -u root -p < mysql/schema.sql
```

Seed data creates 6 users, 5 locations, 4 teams, and 6 players automatically.

### Step 2: Java Backend

Update DB credentials in `TournamentApiServer.java`:
```java
private static final String DB_URL  = "jdbc:mysql://localhost:3306/tournament_db";
private static final String DB_USER = "root";
private static final String DB_PASS = "your_password";
```

Build and run:
```bash
cd java-backend
mvn clean package -q
java -jar target/tournament-backend-1.0.0.jar
# OR during development:
mvn exec:java
```

API starts at: **http://localhost:4567**

### Step 3: Streamlit Frontend

```bash
cd streamlit-frontend
pip install -r requirements.txt
streamlit run app.py
```

Frontend starts at: **http://localhost:8501**

---

## 🌐 REST API Endpoints

| Method | Endpoint                        | Actor       | Description                                  |
|--------|---------------------------------|-------------|----------------------------------------------|
| GET    | /health                         | —           | Health check                                 |
| GET    | /tournaments                    | Viewer      | List all tournaments                         |
| POST   | /tournaments                    | Admin       | Create tournament (validates sport via factory)|
| GET    | /teams                          | Viewer      | List all teams with player count             |
| POST   | /teams                          | TeamManager | Create new team                              |
| GET    | /teams/:teamId/players          | Viewer      | Get players of a team                        |
| POST   | /players                        | TeamManager | Add player to team                           |
| POST   | /registrations                  | TeamManager | Register team + auto-trigger payment         |
| GET    | /registrations/:tournamentId    | Viewer      | Get registrations for a tournament           |
| GET    | /matches?tournament_id=N        | Viewer      | List matches (optionally filtered)           |
| POST   | /matches                        | Organizer   | Schedule a match                             |
| POST   | /scores                         | Referee     | Update score + mark match completed          |
| GET    | /standings/:tournamentId        | Viewer      | Get tournament standings (MySQL VIEW)        |
| GET    | /users?role=X                   | —           | List users filtered by role                  |
| GET    | /locations                      | —           | List all venues                              |

---

## 🖥️ Streamlit UI Pages

| Page              | Actor       | Features                                               |
|-------------------|-------------|--------------------------------------------------------|
| 🏠 Home           | All         | Dashboard: metrics, tournament cards, recent matches  |
| 🏆 Create Tourn.  | Admin       | Form with sport factory validation, existing list      |
| 👥 Register Team  | TeamManager | 3 tabs: register for tournament, create team, add player|
| 📅 Schedule Match | Organizer   | Assign referee, venue, date/time                      |
| 📊 Update Score   | Referee     | Score entry, winner declaration, scoreboards          |
| 🥇 View Results   | Viewer      | Schedule/results, standings table, registrations       |

---

## 🗄️ Database Schema (Key Tables)

```sql
users          → user_id, name, email, role (Admin/Organizer/TeamManager/Referee/Viewer)
tournaments    → tournament_id, name, sport_type, start_date, end_date, status
teams          → team_id, team_name, manager_id
players        → player_id, name, age, skill, team_id
registrations  → registration_id, tournament_id, team_id, status
payments       → payment_id, registration_id, amount, status, gateway_ref
matches        → match_id, tournament_id, team1_id, team2_id, referee_id, match_date, status
scores         → score_id, match_id, team1_score, team2_score, winner_team_id
locations      → location_id, venue_name, city

-- Computed view:
standings      → wins/losses/draws per team per tournament
```

---

## 🧱 OOP Design

```
                    User (abstract)
                   /    |     |    \  \
              Admin  Organizer  TeamManager  Referee  Viewer
              
              Sport (interface)
             /        |          \
          Cricket   Football   Badminton
```

- **Inheritance**: All 5 actors extend `User`, override `showDashboard()`
- **Interface**: `Sport` implemented by 3 sport classes
- **Encapsulation**: Private fields, public getters/setters in all domain classes
- **Polymorphism**: `SportFactory.createSport()` returns `Sport` reference

---

## 🔧 Technology Stack

| Layer     | Technology                         |
|-----------|------------------------------------|
| Frontend  | Python 3.11+, Streamlit, Requests  |
| Backend   | Java 11+, Spark Java 2.9, Gson     |
| Database  | MySQL 8.0+                         |
| Build     | Maven 3.8+                         |
