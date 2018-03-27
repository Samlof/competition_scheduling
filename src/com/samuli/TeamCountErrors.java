package com.samuli;

import java.util.ArrayList;

public class TeamCountErrors {
    // This tracks how many games each team has in this round
    private int[] teamGameCounts;

    // Tracks the error count if team has over 2 or more games in this round
    private int totalGameErrors;
    private int[] errorsByTeam;

    // This should be a reference to Round::games list. Don't edit it from this class!
    private final ArrayList<Game> games;

    public TeamCountErrors(ArrayList<Game> pGames) {
        games = pGames;
        teamGameCounts = Team.getIntArray();
        errorsByTeam = Team.getIntArray();

        // Set up the team count errors
        // Empty round has no errors
        totalGameErrors = 0;

        for (int i = 0; i < errorsByTeam.length; i++) {
            errorsByTeam[i] = 0;
        }
    }

    public TeamCountErrors clone(ArrayList<Game> pGames) {
        TeamCountErrors output = new TeamCountErrors(pGames);
        // Clone the error limits
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
            totalGameErrors++;
            errorsByTeam[id]++;
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
            totalGameErrors--;
            errorsByTeam[id]--;
        } else if (teamGameCounts[id] == 0) {
            // We should never get here, so print a message to show if it happened
            System.out.println("TeamCountErrors::removeTeam tried to remove a team without any games!");
        }
        teamGameCounts[id]--;
    }

    public int getTotalTeamCountErrors() {
        return totalGameErrors;
    }

    public int getTeamCountsErrorByGame(Game game) {
        return errorsByTeam[game.home.id] + errorsByTeam[game.guest.id];
    }

    // For debugging
    public boolean check() {
        for (int i = 0; i < teamGameCounts.length; i++) {
            int c = teamGameCounts[i];
            if (c == 0) {
                if (errorsByTeam[i] != 0) {
                    System.out.println("if(errorsByTeam[i] != 1) {");
                    return false;
                }
            }
            if (c > 1) {
                if (errorsByTeam[i] != c - 1) {
                    System.out.println("if(errorsByTeam[i] != c - 1) {");
                    return false;
                }
            }
            if (c == 1) {
                if (errorsByTeam[i] != 0) {
                    System.out.println("if(errorsByTeam[i] != 0 {");
                    return false;
                }
            }
        }
        int total = 0;
        for (int e : errorsByTeam) {
            total += e;
        }
        if (totalGameErrors != total) {
            System.out.println(totalGameErrors + "(totalGameErrors != total) {" + total);
            return false;
        }
        return true;
    }
}
