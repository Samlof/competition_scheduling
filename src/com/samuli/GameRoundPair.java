package com.samuli;

public class GameRoundPair {
    public Game game;
    public int error;
    public Round round;

    public GameRoundPair(Game _game, Round _round, int _error) {
        game = _game;
        error = _error;
        round = _round;
    }

    public GameRoundPair(Game _game, Round _round) {
        game = _game;
        error = 0;
        round = _round;
    }

    public String description() {
        return "Game:" + game + ", error: " + error + ", Round:" + round;
    }
}
