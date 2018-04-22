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

    public void addGame(Game game) {
        addTeam(game.home);
        addTeam(game.guest);
    }

    public void removeGame(Game game) {
        removeTeam(game.home);
        removeTeam(game.guest);
    }

    private void addTeam(Team t) {
        gameCounts[t.id]++;
        update(t);
    }

    private void removeTeam(Team t) {
        gameCounts[t.id]--;
        update(t);
    }

    public void update(Team t) {
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
