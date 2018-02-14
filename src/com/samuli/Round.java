package com.samuli;

import java.util.ArrayList;


public class Round {
    ArrayList<Game> games;
    TeamGameCountError teamGameCountError;

    public Round() {
        games = new ArrayList<>();

        teamGameCountError = new TeamGameCountError(games);
    }

    public Round clone() {
        Round r = new Round();
        for (Game g : games) {
            r.addGame(g);
        }
        return r;
    }

    public void addGame(Game game) {
        games.add(game);
        teamGameCountError.addGame(game);
    }

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
        // The index in games Array will be the game's unique id for this and chooseGameFromErrorArray function
        // All the error classes use a reference to this same games list, so the order they use will be same as well
        double[] errorsByGame = new double[games.size()];
        for (int i = 0; i < errorsByGame.length; i++) {
            errorsByGame[i] = 0;
        }

        // GameCountErrors

        double[] gameCountErrors = teamGameCountError.getErrorsByGame();
        for (int i = 0; i < games.size(); i++) {
            errorsByGame[i] += gameCountErrors[i];
        }

        return chooseGameFromErrorArray(errorsByGame);
    }

    public double getTotalErrors() {
        double error = 0;
        error += teamGameCountError.getTotalErrors();
        return error;
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
        Game chosenGame = games.size() == 1 ? games.get(0) : games.get(highestGames.get(Globals.randomGen.nextInt(highestGames.size())));

        return new GameRoundPair(
                chosenGame,
                this,
                highest);
    }

    public String description() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total games: " + games.size() + "\n");
        for (Game game : games) {
            sb.append(game.description() + "\n");
        }
        return sb.toString();
    }
}
