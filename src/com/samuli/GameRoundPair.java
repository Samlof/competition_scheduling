package com.samuli;

public class GameRoundPair {
    public Game game;
    public double error;
    public Round round;

    public GameRoundPair(Game _game, Round _round, double _error) {
        game = _game;
        error = _error;
        round = _round;
    }

    public GameRoundPair(Game _game, Round _round) {
        game = _game;
        error = 0;
        round = _round;
    }
}
