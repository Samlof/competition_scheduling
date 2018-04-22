package com.samuli;

public class BreakErrors {

    private final int[] gameCounts;
    private Round nextRound;
    private Round prevRound;

    public BreakErrors() {
        gameCounts = Team.getIntArray();
    }


    public void setNextAndPrevRounds(Round next, Round previous) {
        nextRound = next;
        prevRound = previous;
    }

    public void addTeam(Team t) {
        gameCounts[t.id]++;
        // TODO:
    }

    public void removeTeam(Team t) {
        gameCounts[t.id]--;
        // TODO:
    }


    public int getErrorByGame(Game g) {
        // TODO:
        return 0;
    }

    public int getTotalErrors() {
        // TODO:
        int error = 0;
        for (int i = 0; i < gameCounts.length; i++) {
        }
        return error;
    }

}
