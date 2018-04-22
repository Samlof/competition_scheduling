package com.samuli;

import java.util.ArrayList;

public class Population {
    public final ArrayList<Round> rounds;
    private final TabuList tabuList;

    public Population(ArrayList<Round> pRounds) {
        // Clones the given rounds array
        rounds = new ArrayList<>();
        // First fill the rounds array
        for (int i = 0; i < pRounds.size(); i++) {
            rounds.add(new Round());
        }
        // Set up prev and next round links
        for (int i = 1; i < rounds.size(); i++) {
            Round prevRound = rounds.get(i - 1);
            Round nextRound = null;
            if (i < rounds.size() - 1) {
                nextRound = rounds.get(i + 1);
            }
            rounds.get(i).setNextAndPrevRounds(nextRound, prevRound);
        }
        // Copy the rounds
        for (int i = 0; i < rounds.size(); i++) {
            rounds.get(i).copyRoundsAndErrors(pRounds.get(i));
        }
        tabuList = new TabuList();
    }

    public Population clone() {
        return new Population(rounds);
    }

    private GameRoundPair findGameToMove() {
        int biggestError = 0;
        ArrayList<GameRoundPair> maxGames = new ArrayList<>();
        // Find the games with biggest error
        for (Round round : rounds) {
            GameRoundPair gameRoundPair = round.getHighestErrorGame();
            // The round has no games we can choose from, so skip it
            if (gameRoundPair == null) continue;

            if (gameRoundPair.error > biggestError) {
                biggestError = gameRoundPair.error;
                maxGames.clear();
                maxGames.add(gameRoundPair);
            } else if (gameRoundPair.error == biggestError) {
                maxGames.add(gameRoundPair);
            }
        }
        if (maxGames.size() == 0) {
            // We already have the solution, or every possible bad game is in tabulist
            return getRandomGame();
        }
        int randIndex = Globals.randomGen.nextInt(maxGames.size());
        return maxGames.get(randIndex);
    }

    public GameRoundPair getRandomGame() {
        Round round = getRandomRound();
        // In case the round we got has 0 games in it. Then random another one
        while (round.games.size() == 0) {
            round = getRandomRound();
        }
        return new GameRoundPair(round.getRandomGame(), round);
    }

    public Round getRandomRound() {
        return rounds.get(Globals.randomGen.nextInt(rounds.size()));
    }

    public int getTotalError() {
        int total = 0;
        for (Round round : rounds) {
            total += round.getTotalErrorsWithMods();
        }
        total += checkBreakError();
        return total;
    }

    public void addGame(Round r, Game g) {
        r.addGame(g);
    }

    public void addBoundGame(Round r, Game g) {
        r.addBoundGame(g);
    }

    public int getSoftError() {
        int total = 0;
        for (Round r : rounds) {
            total += r.getSoftErrors();
        }
        return total;
    }

    public int getHardError() {
        int total = 0;
        for (Round r : rounds) {
            total += r.getHardErrors();
        }
        return total;
    }

    public int getTeamCountError() {
        int total = 0;
        for (Round r : rounds) {
            total += r.getTeamCountError();
        }
        return total;
    }

    public int getAwayErrors() {
        int total = 0;
        for (Round r : rounds) {
            total += r.getAwayErrors();
        }
        return total;
    }

    public int getHomeErrors() {
        int total = 0;
        for (Round r : rounds) {
            total += r.getHomeErrors();
        }
        return total;
    }

    public int getBreakErrors() {
        int total = 0;
        for (Round r : rounds) {
            total += r.getBreakErrors();
        }
        return total;
    }
    public void removeGame(Round r, Game g) {
        r.removeGame(g);
    }

    public void develop() {
        GameRoundPair gameToMove = findGameToMove();
        removeGame(gameToMove.round, gameToMove.game);

        Round newRound = findBestRoundForGame(gameToMove.game, gameToMove.round);
        int oldError = getTotalError();
        addGame(newRound, gameToMove.game);
        int newError = getTotalError();
        // Check if the move was good or bad, so should we cancel it
        if (newError > oldError && Globals.sa.accept() == false) {
            removeGame(newRound, gameToMove.game);
            addGame(gameToMove.round, gameToMove.game);
        } else {
            tabuList.addTabu(gameToMove.round, gameToMove.game);
        }
    }

    public void mutate() {
        // Get a random game and round
        GameRoundPair game = getRandomGame();
        Round newRound = getRandomRound();
        // Move it to the random round
        removeGame(game.round, game.game);
        addGame(newRound, game.game);
        tabuList.addTabu(game.round, game.game);
    }

    public void startFromRandomDevelop() {
        // Save old error value
        double oldError = getTotalError();
        // Get random game
        GameRoundPair game = getRandomGame();

        removeGame(game.round, game.game);

        Round newRound = findBestRoundForGame(game.game, game.round);
        // Move it to the best place
        addGame(newRound, game.game);
        // Check new error value and test if it's better or worse
        double newError = getTotalError();
        // If the move made the solution better, or whether sa accepts it
        if (newError < oldError || Globals.sa.accept()) {
            // Add the move to tabulist
            tabuList.addTabu(game.round, game.game);
        } else {
            // Otherwise cancel the move
            removeGame(newRound, game.game);
            addGame(game.round, game.game);
        }
    }

    private Round findBestRoundForGame(Game g, Round roundToSkip) {
        if (g == null) {
            System.out.println("findBestRoundForGame:: Was given a null game");
        }
        int minError = Integer.MAX_VALUE;
        ArrayList<Round> minRounds = new ArrayList<>();
        for (Round r : rounds) {
            if (r == roundToSkip) continue;
            if (tabuList.isInList(r, g)) continue;
            addGame(r, g);
            int newError = getTotalError();
            if (newError < minError) {
                minRounds.clear();
                minRounds.add(r);
                minError = newError;
            } else if (newError == minError) {
                minRounds.add(r);
            }

            removeGame(r, g);
        }

        // if minRounds is empty, it'll crash. Won't happen for now but maybe sometimes
        if (minRounds.size() == 0) {
            System.out.println("Population.findBestRoundForGame had minRounds size of 0");
        }

        // If there is only one, return it. If multiple random one
        return minRounds.size() == 1 ? minRounds.get(0)
                : minRounds.get(Globals.randomGen.nextInt(minRounds.size()));
    }

    public void print() {
        for (Round r : rounds) {
            System.out.println(r.description());
        }
    }

    public int checkBreakError() {
        int error = 0;
        for (Team t : Team.teams) {
            Boolean homeStreak = false;
            Boolean awayStreak = false;

            for (Round r : rounds) {
                for (Game g : r.games) {
                    if (g.guest == t) {
                        if (awayStreak) error++;
                        awayStreak = true;
                        homeStreak = false;
                    }
                    if (g.home == t) {
                        if (homeStreak) error++;
                        awayStreak = false;
                        homeStreak = true;
                    }
                }
            }
        }
        return error;
    }
}
