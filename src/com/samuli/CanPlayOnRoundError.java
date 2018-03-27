package com.samuli;

public class CanPlayOnRoundError {

    private final int[] gameLimits;

    public CanPlayOnRoundError() {
        gameLimits = Team.getIntArray();
    }

    public void setTeam(Team t) {
        gameLimits[t.id] = 1;
    }

    public int hasTeam(Team t) {
        return gameLimits[t.id];
    }
}
