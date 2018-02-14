package com.samuli;

public class SA {
    private final double START_PROB = 1.00;
    private final double END_PROB = 0.00;

    private int markov;
    private int called;
    private double runningProb;
    private double decrInProb;

    SA() {
        markov = Constants.LOOP_AMOUNT / Constants.SA_UPDATE_INTERVAL;
        decrInProb = (START_PROB - END_PROB) / Constants.SA_UPDATE_INTERVAL;
        runningProb = START_PROB;
    }

    public boolean accept() {
        return Globals.randomGen.nextDouble() < runningProb;
    }

    public void calcNewProb() {
        called++;
        if (called == markov) {
            runningProb -= decrInProb;
            called = 0;
        }
        if (runningProb < 0) runningProb = 0;
    }
}
