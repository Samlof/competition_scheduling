package com.samuli;

public class CanPlayOnRoundError {

    private final int[] gameLimits;
    private final int[] gameCounts;

    public CanPlayOnRoundError() {
        gameLimits = Team.getIntArray();
        gameCounts = Team.getIntArray();
    }

    public void setTeam(Team t) {
        gameLimits[t.id] = 1;
    }

    public int hasTeam(Team t) {
        return gameLimits[t.id];
    }

    public void addTeam(Team t) {
        gameCounts[t.id]++;
    }

    public void removeTeam(Team t) {
        gameCounts[t.id]--;
    }

    public int getErrorByTeam(Team t) {
        return gameLimits[t.id] * gameCounts[t.id];
    }
}
