package com.samuli;

public class CanPlayOnRoundErrors {

    private final int[] gameLimits;
    private final int[] gameCounts;

    public CanPlayOnRoundErrors() {
        gameLimits = Team.getIntArray();
        gameCounts = Team.getIntArray();
    }

    public void setTeam(Team t) {
        gameLimits[t.id] = 1;
    }

    public int isTeamSet(Team t) {
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

    public int getTotalErrors() {
        int error = 0;
        for (int i = 0; i < gameCounts.length; i++) {
            error += gameLimits[i] * gameCounts[i];
        }
        return error;
    }
}
