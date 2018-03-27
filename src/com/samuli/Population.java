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

    private GameRoundPair findGameToMove() {
        int biggestError = 0;
        ArrayList<GameRoundPair> maxGames = new ArrayList<>();
        for (Round round : rounds) {
            GameRoundPair gameRoundPair = round.getHighestErrorGame();
            if (gameRoundPair.error > biggestError) {
                biggestError = gameRoundPair.error;
                maxGames.clear();
                maxGames.add(gameRoundPair);
            } else if (gameRoundPair.error == biggestError) {
                maxGames.add(gameRoundPair);
            }
        }
        if (maxGames.size() == 0) {
            // We already have the solution so shouldn't get here. But if we do return a random so no null pointers
            return getRandomGame();
        }
        int randIndex = Globals.randomGen.nextInt(maxGames.size());
        return maxGames.get(randIndex);
    }

    public GameRoundPair getRandomGame() {
        Round round = rounds.get(Globals.randomGen.nextInt(rounds.size()));
        // In case the round we got has 0 games in it. Then random another one
        while (round.games.size() == 0) {
            round = rounds.get(Globals.randomGen.nextInt(rounds.size()));
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

        removeGame(gameToMove.round, gameToMove.game);
        tabuList.addTabu(gameToMove.round, gameToMove.game);

        addGame(newRound, gameToMove.game);
    }

    public void mutate() {
        // Get a random game and round
        GameRoundPair game = getRandomGame();
        Round newRound = getRandomRound();
        // Move it to the random round
        removeGame(game.round, game.game);
        addGame(newRound, game.game);
        // Add the move to tabulist
        tabuList.addTabu(newRound, game.game);
    }

    public void startFromRandomDevelop() {
        // Save old error value
        double oldError = getTotalError();
        // Get random game and best place for it
        GameRoundPair game = getRandomGame();
        Round newRound = findBestRoundForGame(game.game, game.round);
        // Move it to the best place
        removeGame(game.round, game.game);
        addGame(newRound, game.game);
        // Check new error value and test if it's better or worse
        double newError = getTotalError();
        // If the move made the solution worse, check SA whether to cancel it
        if ((newError > oldError && Globals.sa.accept()) == false) {
            removeGame(newRound, game.game);
            addGame(game.round, game.game);
        } else {
            // Add the move to tabulist
            tabuList.addTabu(newRound, game.game);
        }

    }

    private Round findBestRoundForGame(Game g, Round roundToSkip) {
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
