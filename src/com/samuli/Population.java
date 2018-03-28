package com.samuli;

import java.util.ArrayList;

public class Population {
    public final ArrayList<Round> rounds;
    private final TabuList tabuList;

    public Population(ArrayList<Round> pRounds) {
        // Clones the given rounds array
        rounds = new ArrayList<>();
        for (Round r : pRounds) {
            rounds.add(r.clone());
        }
        // Add the games to this rounds error lists as well. Nothing right now but in Teema 3.
        for (Round r : rounds) {
            for (Game g : r.getGames()) {
                addErrorCalc(r, g);
            }
            for (Game g : r.getBoundGames()) {
                addErrorCalc(r, g);
            }
        }

        tabuList = new TabuList();
    }

    public Population clone() {
        return new Population(rounds);
    }

    public Population combine(Population other) {
        // TODO: Fix this to calculate by each game, not by round
        // Now the amount of games will change and ruin the schedule
        ArrayList<Round> newRounds = new ArrayList<>();
        for (int i = 0; i < rounds.size(); i++) {
            Round r1 = rounds.get(i);
            Round r2 = other.rounds.get(i);
            // The rounds are equal, so random it
            if (r1.getTotalErrorsWithMods() == r2.getTotalErrorsWithMods()) {
                newRounds.add((Globals.randomGen.nextDouble() < 0.5 ? r1 : r2).clone());
                // r1 has a better one, so add that
            } else if (r1.getTotalErrorsWithMods() < r2.getTotalErrorsWithMods()) {
                newRounds.add(r1.clone());
            } else {
                newRounds.add(r2.clone());
            }
        }
        return new Population(newRounds);
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
        return total;
    }

    public void addGame(Round r, Game g) {
        r.addGame(g);
        addErrorCalc(r, g);
    }

    public void addBoundGame(Round r, Game g) {
        r.addBoundGame(g);
        addErrorCalc(r, g);
    }

    public void addErrorCalc(Round r, Game g) {
        // TODO: not needed yet. But in Teema 3
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
    public void removeGame(Round r, Game g) {
        r.removeGame(g);
    }

    public void develop() {
        GameRoundPair gameToMove = findGameToMove();

        Round newRound = findBestRoundForGame(gameToMove.game, gameToMove.round);
        int oldError = getTotalError();
        removeGame(gameToMove.round, gameToMove.game);
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
        // Get random game and best place for it
        GameRoundPair game = getRandomGame();
        //Round newRound = getRandomRound();
        Round newRound = findBestRoundForGame(game.game, game.round);
        // Move it to the best place
        removeGame(game.round, game.game);
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
        double minError = Double.MAX_VALUE;
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
}
