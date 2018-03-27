package com.samuli;

import java.util.ArrayList;


public class Round {
    public final ArrayList<Game> games;
    public final ArrayList<Game> boundGames;

    private final CanPlayOnRoundErrors homeErrors;
    private final CanPlayOnRoundErrors awayErrors;
    private TeamCountErrors teamCountErrors;


    public Round() {
        games = new ArrayList<>();
        boundGames = new ArrayList<>();

        teamCountErrors = new TeamCountErrors(games);
        awayErrors = new CanPlayOnRoundErrors();
        homeErrors = new CanPlayOnRoundErrors();
    }

    public Round clone() {
        Round output = new Round();
        // Clone the set home and away limits
        for (Team t : Team.teams) {
            if (homeErrors.isTeamSet(t) > 0) {
                output.homeErrors.setTeam(t);
            }
            if (awayErrors.isTeamSet(t) > 0) {
                output.awayErrors.setTeam(t);
            }
        }

        // Clone the games
        for (Game g : games) {
            output.addGame(g);
        }
        for (Game g : boundGames) {
            output.addBoundGame(g);
        }
        return output;
    }

    public ArrayList<Game> getBoundGames() {
        // Copy it into a new array, so cannot accidentally change it outside this class
        ArrayList<Game> output = new ArrayList<>();
        output.addAll(boundGames);
        return output;
    }

    public ArrayList<Game> getGames() {
        // Copy it into a new array, so cannot accidentally change it outside this class
        ArrayList<Game> output = new ArrayList<>();
        output.addAll(games);
        return output;
    }

    public void addGame(Game game) {
        games.add(game);
        addGameToErrors(game);
    }

    public void setAwayGameLimit(Team team) {
        awayErrors.setTeam(team);
    }

    public void setHomeGameLimit(Team team) {
        homeErrors.setTeam(team);
    }

    public void addBoundGame(Game game) {
        boundGames.add(game);
        addGameToErrors(game);
    }

    private void addGameToErrors(Game game) {
        teamCountErrors.addGame(game);
        homeErrors.addTeam(game.home);
        awayErrors.addTeam(game.guest);
    }
    // This should only be called from Population.removeGame!
    public void removeGame(Game game) {
        if (games.remove(game) == false) {
            // If the game didn't exist
            return;
        }
        teamCountErrors.removeGame(game);
        homeErrors.removeTeam(game.home);
        awayErrors.removeTeam(game.guest);
    }

    public Game getRandomGame() {
        return games.get(Globals.randomGen.nextInt(games.size()));
    }

    public GameRoundPair getHighestErrorGame() {
        if (games.size() == 0) {
            // No game to choose from. Return null and skip this
            return null;
        }
        // The index in games Array will be the game's unique id for this and chooseGameFromErrorArray function
        // All the error classes use a reference to this same games list, so the order they use will be same as well
        int[] errorsByGame = new int[games.size()];
        // Calculate the errors for games
        for (int i = 0; i < games.size(); i++) {
            Game g = games.get(i);
            errorsByGame[i] = 0;

            errorsByGame[i] += teamCountErrors.getTeamCountsErrorByGame(g) * Constants.GAME_COUNT_ERROR * Constants.HARD_ERROR;
            // Away and home game limit errors
            errorsByGame[i] += awayErrors.getErrorByTeam(games.get(i).guest) * Constants.AWAY_GAME_ERROR * Constants.HARD_ERROR;
            errorsByGame[i] += homeErrors.getErrorByTeam(games.get(i).home) * Constants.HOME_GAME_ERROR * Constants.HARD_ERROR;
        }
        return chooseGameFromErrorArray(errorsByGame);
    }

    public int getTotalErrorsWithMods() {
        int error = 0;
        error += getTeamCountError() * Constants.GAME_COUNT_ERROR * Constants.HARD_ERROR;
        error += getAwayErrors() * Constants.AWAY_GAME_ERROR * Constants.HARD_ERROR;
        error += getHomeErrors() * Constants.HOME_GAME_ERROR * Constants.HARD_ERROR;
        return error;
    }

    public int getTeamCountError() {
        return teamCountErrors.getTotalTeamCountErrors();
    }

    public int getAwayErrors() {
        return awayErrors.getTotalErrors();
    }

    public int getHomeErrors() {
        return homeErrors.getTotalErrors();
    }

    public int getHardErrors() {
        int error = 0;
        error += getTeamCountError();
        error += getAwayErrors();
        error += getHomeErrors();
        return error;
    }

    public int getSoftErrors() {
        int total = 0;
        return total;
    }

    private GameRoundPair chooseGameFromErrorArray(int[] errorsByGame) {
        // Find highest error
        int highest = 0;
        for (int anErrorsByGame : errorsByGame) {
            if (anErrorsByGame > highest) {
                highest = anErrorsByGame;
            }
        }

        // Find indexes with the highest error
        ArrayList<Integer> highestGames = new ArrayList<>();
        for (int i = 0; i < errorsByGame.length; i++) {
            if (errorsByGame[i] == highest) highestGames.add(i);
        }

        if (highestGames.size() == 0) {
            // Nothing to choose from, meaning all games are in tabulist. Return null so it will just skip
            if (highest == 0) {
                return null;
            }
            // Should never get here!
            System.out.println("highestGames.size() == 0 " + errorsByGame);
            System.out.println("Game count " + games.size());
            // If games.size() is 0, next line throws and exception. There is a check earlier. If this happens still change the fix
        }
        // Choose one of them
        Integer chosenId = highestGames.size() == 1 ? highestGames.get(0) : highestGames.get(Globals.randomGen.nextInt(highestGames.size()));
        Game chosenGame = games.get(chosenId);

        return new GameRoundPair(
                chosenGame,
                this,
                highest);
    }

    public String description() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total games: " + games.size() + ", Total error: " + getTotalErrorsWithMods() + "\n");
        return sb.toString();
    }
}
