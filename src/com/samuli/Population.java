package com.samuli;

import java.util.ArrayList;

public class Population {
    public final ArrayList<Round> rounds;
    private final TabuList tabuList;

    public Population(ArrayList<Round> pRounds) {
        // Clones the given rounds array
        rounds = new ArrayList<>();
        for (int i = 0; i < pRounds.size(); i++) {
            rounds.add(new Round());
            for (Game g : pRounds.get(i).games) {
                addGame(rounds.get(i), g);
            }
        }

        tabuList = new TabuList();
    }

    private GameRoundPair findGameToMove() {
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
        // In case the round we got has 0 games in it. Then random another one
        while (round.games.size() == 0) {
            round = rounds.get(Globals.randomGen.nextInt(rounds.size()));
        }
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

    public void develop() {
        GameRoundPair gameToMove = findGameToMove();
        removeGame(gameToMove.round, gameToMove.game);
        tabuList.addTabu(gameToMove.round, gameToMove.game);

        Round newRound = findBestRoundForGame(gameToMove.game, gameToMove.round);
        addGame(newRound, gameToMove.game);
    }

    public Round findBestRoundForGame(Game g, Round roundToSkip) {
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
