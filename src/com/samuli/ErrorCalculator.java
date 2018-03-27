package com.samuli;

import java.util.ArrayList;

public class ErrorCalculator {
    // This tracks how many games each team has in this round
    private int[] teamGameCounts;

    // Tracks the error count if team has over 2 or more games in this round
    private int totalGameErrors_GameCount;
    private int[] errorsByTeam_GameCount;

    private final CanPlayOnRoundError homeErrors;
    private final CanPlayOnRoundError awayErrors;

    // This should be a reference to Round::games list. Don't edit it from this class!
    private final ArrayList<Game> games;

    public ErrorCalculator(ArrayList<Game> pGames) {
        games = pGames;
        teamGameCounts = Team.getIntArray();
        errorsByTeam_GameCount = Team.getIntArray();

        // Set up the team count errors
        // Empty round has no errors
        totalGameErrors_GameCount = 0;

        for (int i = 0; i < errorsByTeam_GameCount.length; i++) {
            errorsByTeam_GameCount[i] = 0;
        }

        // Home and away game limit errors
        homeErrors = new CanPlayOnRoundError();
        awayErrors = new CanPlayOnRoundError();
    }

    public ErrorCalculator clone(ArrayList<Game> pGames) {
        ErrorCalculator output = new ErrorCalculator(pGames);
        // Clone the error limits
        for (Team t : Team.teams) {
            if (homeErrors.hasTeam(t) > 0) {
                output.homeErrors.setTeam(t);
            }
            if (awayErrors.hasTeam(t) > 0) {
                output.awayErrors.setTeam(t);
            }
        }
        return output;
    }

    public void setAwayGameLimit(Team team) {
        awayErrors.setTeam(team);
    }

    public void setHomeGameLimit(Team team) {
        homeErrors.setTeam(team);
    }

    public int getAwayErrorByTeam(Team t) {
        return getErrorsFromCanPlayErrorByTeam(awayErrors, t);
    }

    public int getAwayErrors() {
        return getErrorsFromCanPlayError(awayErrors);
    }

    public int getHomeErrorByTeam(Team t) {
        return getErrorsFromCanPlayErrorByTeam(homeErrors, t);
    }

    public int getHomeErrors() {
        return getErrorsFromCanPlayError(homeErrors);
    }

    private int getErrorsFromCanPlayErrorByTeam(CanPlayOnRoundError c, Team t) {
        return c.hasTeam(t) * teamGameCounts[t.id];
    }

    private int getErrorsFromCanPlayError(CanPlayOnRoundError c) {
        int output = 0;
        for (Team t : Team.teams) {
            output += getErrorsFromCanPlayErrorByTeam(c, t);
        }
        return output;
    }

    public void addGame(Game game) {
        addTeam(game.home);
        addTeam(game.guest);
    }

    private void addTeam(Team team) {
        int id = team.id;
        if (teamGameCounts[id] > 0) {
            // Already has one game with this team. Add an error
            totalGameErrors_GameCount++;
            errorsByTeam_GameCount[id]++;
        }
        teamGameCounts[id]++;
    }

    public void removeGame(Game game) {
        removeTeam(game.home);
        removeTeam(game.guest);
    }

    private void removeTeam(Team team) {
        int id = team.id;
        if (teamGameCounts[id] > 1) {
            // Already has one game with this team. Remove an error
            totalGameErrors_GameCount--;
            errorsByTeam_GameCount[id]--;
        } else if (teamGameCounts[id] == 0) {
            // We should never get here, so print a message to show if it happened
            System.out.println("ErrorCalculator::removeTeam tried to remove a team without any games!");
        }
        teamGameCounts[id]--;
    }

    public int getTotalTeamCountErrors() {
        return totalGameErrors_GameCount;
    }

    public int[] getTeamCountErrorsAsGamesArray() {
        // Round uses the same games list, the order will be same, so i works as a unique identifier for a game
        int[] output = new int[games.size()];
        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            output[i] = errorsByTeam_GameCount[game.home.id] + errorsByTeam_GameCount[game.guest.id];
        }
        return output;
    }

    // For debugging
    public boolean check() {
        for (int i = 0; i < teamGameCounts.length; i++) {
            int c = teamGameCounts[i];
            if (c == 0) {
                if (errorsByTeam_GameCount[i] != 1) {
                    System.out.println("if(errorsByTeam_GameCount[i] != 1) {");
                    return false;
                }
            }
            if (c > 1) {
                if (errorsByTeam_GameCount[i] != c - 1) {
                    System.out.println("if(errorsByTeam_GameCount[i] != c - 1) {");
                    return false;
                }
            }
            if (c == 1) {
                if (errorsByTeam_GameCount[i] != 0) {
                    System.out.println("if(errorsByTeam_GameCount[i] != 0 {");
                    return false;
                }
            }
        }
        int total = 0;
        for (int e : errorsByTeam_GameCount) {
            total += e;
        }
        if (totalGameErrors_GameCount != total) {
            System.out.println(totalGameErrors_GameCount + "(totalGameErrors_GameCount != total) {" + total);
            return false;
        }
        return true;
    }
}
