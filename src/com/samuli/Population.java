package com.samuli;

import java.util.ArrayList;

public class Population {
    private ArrayList<Round> rounds;
    private TabuList tabuList;

    public Population(ArrayList<Round> pRounds) {
        rounds = new ArrayList<>(pRounds);
        tabuList = new TabuList();
    }

    public GameRoundPair findGameToMove() {
        GameRoundPair max = getRandomGame();
        max.error = 0;
        for (Round round : rounds) {
            GameRoundPair gameRoundPair = round.getHighestErrorGame();
            if (gameRoundPair.error > max.error) max = gameRoundPair;
        }
        return max;
    }

    public GameRoundPair getRandomGame() {
        Round round = rounds.get(Globals.randomGen.nextInt(rounds.size()));
        return new GameRoundPair(round.getRandomGame(), round);
    }

    public double getTotalError() {
        double total = 0;
        for (Round round : rounds) {
            total += round.getTotalErrors();
        }
        return total;
    }

    public void addGame(Round r, Game g) {
        r.addGame(g);
    }

    public void removeGame(Round r, Game g) {
        r.removeGame(g);
    }

    public void move(GameRoundPair gameToMove) {
        removeGame(gameToMove.round, gameToMove.game);
        tabuList.addTabu(gameToMove.round, gameToMove.game);

        Round newRound = findBestRoundForGame(gameToMove.game, gameToMove.round);
        addGame(newRound, gameToMove.game);
    }

    private Round findBestRoundForGame(Game g, Round roundToSkip) {
        double minError = Double.MAX_VALUE;
        ArrayList<Round> minRounds = new ArrayList<>();
        for (Round r : rounds) {
            if (r == roundToSkip) continue;
            if (tabuList.isInList(r, g)) continue;

            addGame(r, g);
            double newError = getTotalError();
            if (newError < minError) {
                minRounds.clear();
                minRounds.add(r);
                minError = newError;
            } else if (newError == minError) {
                minRounds.add(r);
            }

            removeGame(r, g);
        }
        //System.out.println(minRounds.size());
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
