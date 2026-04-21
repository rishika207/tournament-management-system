package com.tournament.model;

public class Cricket implements Sport {
    public String getSportName() { return "Cricket"; }
    public int getMaxPlayersPerTeam() { return 11; }
    public int getMatchDurationMinutes() { return 180; }
    public String getScoringUnit() { return "runs"; }
}