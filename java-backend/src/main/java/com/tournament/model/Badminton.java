package com.tournament.model;

public class Badminton implements Sport {
    public String getSportName() { return "Badminton"; }
    public int getMaxPlayersPerTeam() { return 2; }
    public int getMatchDurationMinutes() { return 45; }
    public String getScoringUnit() { return "points"; }
}