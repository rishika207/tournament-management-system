package com.tournament.model;

import java.time.LocalDate;
import java.time.LocalDateTime;


// ═══════════════════════════════════════════════
//  Tournament
// ═══════════════════════════════════════════════
class Tournament {
    private int       tournamentId;
    private String    name;
    private Sport     sport;
    private LocalDate startDate;
    private LocalDate endDate;
    private String    status;   // Upcoming | Ongoing | Completed

    public Tournament(int tournamentId, String name, Sport sport,
                      LocalDate startDate, LocalDate endDate) {
        this.tournamentId = tournamentId;
        this.name         = name;
        this.sport        = sport;
        this.startDate    = startDate;
        this.endDate      = endDate;
        this.status       = "Upcoming";
    }

    // Getters & Setters
    public int       getTournamentId() { return tournamentId; }
    public String    getName()         { return name; }
    public Sport     getSport()        { return sport; }
    public LocalDate getStartDate()    { return startDate; }
    public LocalDate getEndDate()      { return endDate; }
    public String    getStatus()       { return status; }
    public void      setStatus(String s) { this.status = s; }

    @Override
    public String toString() {
        return String.format("Tournament{id=%d, name='%s', sport=%s, %s→%s, status=%s}",
                tournamentId, name, sport.getSportName(), startDate, endDate, status);
    }
}

// ═══════════════════════════════════════════════
//  Team
// ═══════════════════════════════════════════════
class Team {
    private int    teamId;
    private String teamName;
    private int    managerId;

    public Team(int teamId, String teamName, int managerId) {
        this.teamId    = teamId;
        this.teamName  = teamName;
        this.managerId = managerId;
    }

    public int    getTeamId()    { return teamId; }
    public String getTeamName()  { return teamName; }
    public int    getManagerId() { return managerId; }

    @Override
    public String toString() {
        return String.format("Team{id=%d, name='%s', managerId=%d}", teamId, teamName, managerId);
    }
}

// ═══════════════════════════════════════════════
//  Player
// ═══════════════════════════════════════════════
class Player {
    private int    playerId;
    private String name;
    private int    age;
    private String skill;   // Beginner | Intermediate | Advanced | Pro
    private int    teamId;

    public Player(int playerId, String name, int age, String skill, int teamId) {
        this.playerId = playerId;
        this.name     = name;
        this.age      = age;
        this.skill    = skill;
        this.teamId   = teamId;
    }

    public int    getPlayerId() { return playerId; }
    public String getName()     { return name; }
    public int    getAge()      { return age; }
    public String getSkill()    { return skill; }
    public int    getTeamId()   { return teamId; }

    @Override
    public String toString() {
        return String.format("Player{id=%d, name='%s', age=%d, skill=%s, teamId=%d}",
                playerId, name, age, skill, teamId);
    }
}

// ═══════════════════════════════════════════════
//  Match
// ═══════════════════════════════════════════════
class Match {
    private int           matchId;
    private int           tournamentId;
    private int           team1Id;
    private int           team2Id;
    private int           refereeId;
    private LocalDateTime matchDate;
    private String        status;  // Scheduled | InProgress | Completed | Cancelled

    public Match(int matchId, int tournamentId, int team1Id, int team2Id,
                 int refereeId, LocalDateTime matchDate) {
        this.matchId      = matchId;
        this.tournamentId = tournamentId;
        this.team1Id      = team1Id;
        this.team2Id      = team2Id;
        this.refereeId    = refereeId;
        this.matchDate    = matchDate;
        this.status       = "Scheduled";
    }

    public int           getMatchId()      { return matchId; }
    public int           getTournamentId() { return tournamentId; }
    public int           getTeam1Id()      { return team1Id; }
    public int           getTeam2Id()      { return team2Id; }
    public int           getRefereeId()    { return refereeId; }
    public LocalDateTime getMatchDate()    { return matchDate; }
    public String        getStatus()       { return status; }
    public void          setStatus(String s) { this.status = s; }

    @Override
    public String toString() {
        return String.format("Match{id=%d, tournament=%d, teams=[%d vs %d], referee=%d, date=%s, status=%s}",
                matchId, tournamentId, team1Id, team2Id, refereeId, matchDate, status);
    }
}

// ═══════════════════════════════════════════════
//  Score
// ═══════════════════════════════════════════════
class Score {
    private int    scoreId;
    private int    matchId;
    private String team1Score;
    private String team2Score;
    private int    winnerTeamId;

    public Score(int scoreId, int matchId, String team1Score, String team2Score, int winnerTeamId) {
        this.scoreId      = scoreId;
        this.matchId      = matchId;
        this.team1Score   = team1Score;
        this.team2Score   = team2Score;
        this.winnerTeamId = winnerTeamId;
    }

    public int    getScoreId()      { return scoreId; }
    public int    getMatchId()      { return matchId; }
    public String getTeam1Score()   { return team1Score; }
    public String getTeam2Score()   { return team2Score; }
    public int    getWinnerTeamId() { return winnerTeamId; }

    @Override
    public String toString() {
        return String.format("Score{matchId=%d, %s - %s, winner=%d}",
                matchId, team1Score, team2Score, winnerTeamId);
    }
}

// ═══════════════════════════════════════════════
//  Registration
// ═══════════════════════════════════════════════
class Registration {
    private int    registrationId;
    private int    tournamentId;
    private int    teamId;
    private String status;  // Pending | Confirmed | Rejected

    public Registration(int registrationId, int tournamentId, int teamId) {
        this.registrationId = registrationId;
        this.tournamentId   = tournamentId;
        this.teamId         = teamId;
        this.status         = "Pending";
    }

    public int    getRegistrationId() { return registrationId; }
    public int    getTournamentId()   { return tournamentId; }
    public int    getTeamId()         { return teamId; }
    public String getStatus()         { return status; }
    public void   setStatus(String s) { this.status = s; }

    @Override
    public String toString() {
        return String.format("Registration{id=%d, tournament=%d, team=%d, status=%s}",
                registrationId, tournamentId, teamId, status);
    }
}

// ═══════════════════════════════════════════════
//  Payment
// ═══════════════════════════════════════════════
class Payment {
    private int    paymentId;
    private int    registrationId;
    private double amount;
    private String status;       // Pending | Completed | Failed
    private String gatewayRef;

    public Payment(int paymentId, int registrationId, double amount) {
        this.paymentId      = paymentId;
        this.registrationId = registrationId;
        this.amount         = amount;
        this.status         = "Pending";
        this.gatewayRef     = null;
    }

    public int    getPaymentId()      { return paymentId; }
    public int    getRegistrationId() { return registrationId; }
    public double getAmount()         { return amount; }
    public String getStatus()         { return status; }
    public String getGatewayRef()     { return gatewayRef; }
    public void   setStatus(String s) { this.status = s; }
    public void   setGatewayRef(String ref) { this.gatewayRef = ref; }

    @Override
    public String toString() {
        return String.format("Payment{id=%d, regId=%d, amount=%.2f, status=%s, ref=%s}",
                paymentId, registrationId, amount, status, gatewayRef);
    }
}

// ═══════════════════════════════════════════════
//  Location
// ═══════════════════════════════════════════════
class Location {
    private int    locationId;
    private String venueName;
    private String city;

    public Location(int locationId, String venueName, String city) {
        this.locationId = locationId;
        this.venueName  = venueName;
        this.city       = city;
    }

    public int    getLocationId() { return locationId; }
    public String getVenueName()  { return venueName; }
    public String getCity()       { return city; }

    @Override
    public String toString() {
        return String.format("Location{id=%d, venue='%s', city='%s'}", locationId, venueName, city);
    }
}
