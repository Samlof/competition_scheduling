package com.samuli;


import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        makeGamesAndRounds();

        Population[] populations = new Population[Constants.POPULATION_COUNT];
        for (int i = 0; i < populations.length; i++) {
            populations[i] = makePopulation();
        }

        double lowestError = Double.MAX_VALUE;
        int lowestRound = 0;
        Population lowestPop = null;

        //populations[0].print();
        for (int roundNr = 0; roundNr < Constants.LOOP_AMOUNT; roundNr++) {
            // Develop solution
            for (int betteringRounds = 0; betteringRounds < Constants.BETTERING_ROUNDS; betteringRounds++) {
                // Move the worst scored game BETTERING_ROUNDS times
                populations[0].develop();
            }
            Globals.sa.calcNewProb();

            // Mutate
            // Move this inside Population as method?
            Population p = populations[0];
            double oldError = p.getTotalError();
            GameRoundPair game = p.getRandomGame();
            Round newRound = p.findBestRoundForGame(game.game, game.round);
            p.removeGame(game.round, game.game);
            p.addGame(newRound, game.game);
            double newError = p.getTotalError();
            // If the move made the solution worse, check SA whether to cancel it
            if ((newError > oldError && Globals.sa.accept()) == false) {
                p.removeGame(newRound, game.game);
                p.addGame(game.round, game.game);
            }

            // TODO: Make use of the population


            // Save the best round
            if (p.getTotalError() < lowestError) {
                lowestError = p.getTotalError();
                lowestRound = roundNr;
                // TODO: Save it!
                lowestPop = new Population(p.rounds);
            }
            //System.out.println("Roundnr: " + roundNr + " total error: " + populations[0].getTotalError());
        }
        System.out.println("lowestError " + lowestError + ", round: " + lowestRound);
        System.out.println("------------------End-------------");
    }


    private static ArrayList<Game> games = new ArrayList<>();
    private static ArrayList<Round> baseRounds = new ArrayList<>();

    private static Population makePopulation() {
        Population output = new Population(baseRounds);
        // Add the games to a random round
        for (Game game : games) {
            Round round = output.rounds.get(Globals.randomGen.nextInt(output.rounds.size()));
            output.addGame(round, game);
        }
        return output;
    }

    private static void makeGamesAndRounds() {
        // Create teams
        // Teema 1 has 12 teams
        for (int i = 0; i < 12; i++) {
            Team a = new Team("" + i);
        }

        // Create games from teams for 2RR
        ArrayList<Team> teams = Team.teams;
        for (Team team1 : teams) {
            for (Team team2 : teams) {
                if (team1 == team2) continue;
                games.add(new Game(team1, team2));
            }
        }

        // Create empty rounds
        // Teema 1 has (12 - 1) * 2 = 22 rounds
        for (int j = 0; j < 22; j++) {
            baseRounds.add(new Round());
        }

        // TODO: handle bound games
    }
}
