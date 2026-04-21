package com.tournament.model;

public interface Sport {
    String getSportName();
    int getMaxPlayersPerTeam();
    int getMatchDurationMinutes();
    String getScoringUnit();
}