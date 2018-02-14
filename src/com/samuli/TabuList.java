package com.samuli;

public class TabuList {

    private class Tabu {
        Round round;
        Game game;

        Tabu(Round pRound, Game pGame) {
            round = pRound;
            game = pGame;
        }
    }

    private Tabu[] tabuArray;
    private int currIndex;

    public TabuList() {
        tabuArray = new Tabu[Constants.TABULIST_SIZE];
        currIndex = 0;
    }

    public void addTabu(Round round, Game game) {
        if (currIndex == tabuArray.length) currIndex = 0;
        tabuArray[currIndex] = new Tabu(round, game);
        currIndex++;
    }

    public boolean isInList(Round round, Game game) {
        for (Tabu t : tabuArray) {
            if (t != null && t.round == round && t.game == game) return true;
        }
        return false;
    }
}
