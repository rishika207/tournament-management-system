package com.tournament.model;

// ═══════════════════════════════════════════════
//  ACTOR 1 — Admin
// ═══════════════════════════════════════════════
class Admin extends User {
    public Admin(int userId, String name, String email) {
        super(userId, name, email, "Admin");
    }

    @Override
    public void showDashboard() {
        System.out.println("=== Admin Dashboard ===");
        System.out.println("• Create / delete tournaments");
        System.out.println("• Manage all users");
        System.out.println("• View system reports");
    }

    public void createTournament(String tournamentName) {
        System.out.println(name + " created tournament: " + tournamentName);
    }

    public void deleteUser(int targetUserId) {
        System.out.println(name + " deleted user with id: " + targetUserId);
    }
}

// ═══════════════════════════════════════════════
//  ACTOR 2 — Organizer
// ═══════════════════════════════════════════════
class Organizer extends User {
    public Organizer(int userId, String name, String email) {
        super(userId, name, email, "Organizer");
    }

    @Override
    public void showDashboard() {
        System.out.println("=== Organizer Dashboard ===");
        System.out.println("• Schedule matches");
        System.out.println("• Assign referees");
        System.out.println("• Manage locations");
    }

    public void scheduleMatch(int matchId) {
        System.out.println(name + " scheduled match #" + matchId);
    }

    public void assignReferee(int matchId, int refereeId) {
        System.out.println(name + " assigned referee #" + refereeId + " to match #" + matchId);
    }
}

// ═══════════════════════════════════════════════
//  ACTOR 3 — TeamManager
// ═══════════════════════════════════════════════
class TeamManager extends User {
    public TeamManager(int userId, String name, String email) {
        super(userId, name, email, "TeamManager");
    }

    @Override
    public void showDashboard() {
        System.out.println("=== Team Manager Dashboard ===");
        System.out.println("• Register team for tournaments");
        System.out.println("• Add / remove players");
        System.out.println("• View team stats");
    }

    public void registerTeam(int teamId, int tournamentId) {
        System.out.println(name + " registered team #" + teamId + " for tournament #" + tournamentId);
    }

    public void addPlayer(Player player, int teamId) {
        System.out.println(name + " added player '" + player.getName() + "' to team #" + teamId);
    }
}

// ═══════════════════════════════════════════════
//  ACTOR 4 — Referee
// ═══════════════════════════════════════════════
class Referee extends User {
    public Referee(int userId, String name, String email) {
        super(userId, name, email, "Referee");
    }

    @Override
    public void showDashboard() {
        System.out.println("=== Referee Dashboard ===");
        System.out.println("• Update live scores");
        System.out.println("• Declare match results");
        System.out.println("• View assigned matches");
    }

    public void updateScore(int matchId, String team1Score, String team2Score) {
        System.out.printf("%s updated score for match #%d: %s - %s%n",
                name, matchId, team1Score, team2Score);
    }

    public void declareResult(int matchId, int winnerTeamId) {
        System.out.println(name + " declared winner team #" + winnerTeamId + " for match #" + matchId);
    }
}

// ═══════════════════════════════════════════════
//  ACTOR 5 — Viewer
// ═══════════════════════════════════════════════
class Viewer extends User {
    public Viewer(int userId, String name, String email) {
        super(userId, name, email, "Viewer");
    }

    @Override
    public void showDashboard() {
        System.out.println("=== Viewer Dashboard ===");
        System.out.println("• Browse tournaments");
        System.out.println("• View match schedules");
        System.out.println("• View live scores & standings");
    }

    public void viewSchedule(int tournamentId) {
        System.out.println(name + " is viewing schedule for tournament #" + tournamentId);
    }

    public void viewStandings(int tournamentId) {
        System.out.println(name + " is viewing standings for tournament #" + tournamentId);
    }
}
