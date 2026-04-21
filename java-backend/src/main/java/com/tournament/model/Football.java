package com.tournament.model;

public class Football implements Sport {
    public String getSportName() { return "Football"; }
    public int getMaxPlayersPerTeam() { return 11; }
    public int getMatchDurationMinutes() { return 90; }
    public String getScoringUnit() { return "goals"; }
}