package com.samuli;

import java.util.ArrayList;

public class TeamGameCountError {
    private int[] teamGameCounts;
    private int totalGameErrors;
    private int[] errorsByTeam;
    // This should be a reference to Round::games list. Don't edit it from this class!
    private final ArrayList<Game> games;

    public TeamGameCountError(ArrayList<Game> pGames) {
        games = pGames;
        teamGameCounts = Team.getIntArray();
        errorsByTeam = Team.getIntArray();

        // Teema 1 wants 1 game per team. So empty round has that many errors
        totalGameErrors = Team.getTeamCount();

        // Similarly. Teema 1 starts with 1 error per team
        for (int i = 0; i < errorsByTeam.length; i++) {
            errorsByTeam[i] = 1;
        }
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
        } else if (teamGameCounts[id] == 0) {
            // Teema 1 wants 1 game per team per game, so remove one error here
            totalGameErrors--;
            errorsByTeam[id]--;
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
            // Already has one game with this team. Add an error
            totalGameErrors--;
            errorsByTeam[id]--;
        } else if (teamGameCounts[id] == 1) {
            // Teema 1 wants 1 game per team per game, so remove one error here
            totalGameErrors++;
            errorsByTeam[id]++;
        } else {
            // We should never get here, so print a message to show if it happened
            System.out.println("TeamGameCountError::removeTeam tried to remove a team without any games!");
        }
        teamGameCounts[id]--;
    }

    public int getTotalErrors() {
        return totalGameErrors;
    }

    public double[] getErrorsByGame() {
        // Round uses the same games list, the order will be same, so i works as a unique identifier for a game
        double[] output = new double[games.size()];
        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            output[i] = errorsByTeam[game.home.id] + errorsByTeam[game.guest.id];
        }
        return output;
    }

    // For debugging
    public boolean check() {
        for (int i = 0; i < teamGameCounts.length; i++) {
            int c = teamGameCounts[i];
            if (c == 0) {
                if (errorsByTeam[i] != 1) {
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
