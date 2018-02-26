package com.samuli;

import java.util.ArrayList;


public class Round {
    public final ArrayList<Game> games;
    private final TeamGameCountError teamGameCountError;

    public Round() {
        games = new ArrayList<>();

        teamGameCountError = new TeamGameCountError(games);
    }

    // This should only be called from Population.addGame!
    public void addGame(Game game) {
        games.add(game);
        teamGameCountError.addGame(game);
    }

    // This should only be called from Population.removeGame!
    public void removeGame(Game game) {
        if (games.remove(game) == false) {
            // If the game didn't exist
            return;
        }
        teamGameCountError.removeGame(game);
    }

    public Game getRandomGame() {
        return games.get(Globals.randomGen.nextInt(games.size()));
    }

    public GameRoundPair getHighestErrorGame() {
        if (games.size() == 0) {
            // No game to choose from. Skip even earlier in the chain? Or this workaround
            return new GameRoundPair(null, this, 0);
        }
        // The index in games Array will be the game's unique id for this and chooseGameFromErrorArray function
        // All the error classes use a reference to this same games list, so the order they use will be same as well
        double[] errorsByGame = new double[games.size()];
        for (int i = 0; i < errorsByGame.length; i++) {
            errorsByGame[i] = 0;
        }

        // GameCountErrors

        double[] gameCountErrors = teamGameCountError.getErrorsByGame();
        for (int i = 0; i < games.size(); i++) {
            errorsByGame[i] += gameCountErrors[i] * Constants.GAME_COUNT_ERROR * Constants.HARD_ERROR;
        }

        return chooseGameFromErrorArray(errorsByGame);
    }

    public double getTotalErrors() {
        double error = 0;
        error += teamGameCountError.getTotalErrors() * Constants.GAME_COUNT_ERROR * Constants.HARD_ERROR;
        return error;
    }

    public int getTeamCountError() {
        return teamGameCountError.getTotalErrors();
    }

    public int getHardErrors() {
        int total = 0;
        total += teamGameCountError.getTotalErrors();
        return total;
    }

    public int getSoftErrors() {
        int total = 0;
        return total;
    }

    private GameRoundPair chooseGameFromErrorArray(double[] errorsByGame) {
        // Find highest error
        double highest = 0;
        for (double anErrorsByGame : errorsByGame) {
            if (anErrorsByGame > highest) {
                highest = anErrorsByGame;
            }
        }

        // Find games with the highest error
        ArrayList<Integer> highestGames = new ArrayList<>();
        for (int i = 0; i < errorsByGame.length; i++) {
            if (errorsByGame[i] == highest) highestGames.add(i);
        }
        // Choose one of them
        if (highestGames.size() == 0) {
            System.out.println("highestGames.size() == 0 " + errorsByGame);
            System.out.println("Game count " + games.size());
            // If games.size() is 0, next line throws and exception. There is a check earlier. If this happens still change the fix
        }
        Integer chosenId = highestGames.size() == 1 ? highestGames.get(0) : highestGames.get(Globals.randomGen.nextInt(highestGames.size()));
        Game chosenGame = games.get(chosenId);

        return new GameRoundPair(
                chosenGame,
                this,
                highest);
    }

    public String description() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total games: " + games.size() + ", Total error: " + getTotalErrors() + "\n");
        return sb.toString();
    }
}
