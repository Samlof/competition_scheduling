package com.samuli;


import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        makeGamesAndRounds();

        Population[] populations = new Population[Constants.POPULATION_COUNT];
        for (int i = 0; i < populations.length; i++) {
            populations[i] = makePopulation();
        }

        //populations[0].print();
        for (int roundNr = 0; roundNr < Constants.ROUND_AMOUNT; roundNr++) {
            // First move a random game
            populations[0].move(populations[0].getRandomGame());
            for (int betteringRounds = 0; betteringRounds < Constants.BETTERING_ROUNDS; betteringRounds++) {
                // Move the worst scored game 8 times
                populations[0].move(populations[0].findGameToMove());
            }
            System.out.println("Roundnr: " + roundNr + " total error: " + populations[0].getTotalError());
        }

        //populations[0].print();
    }


    private static ArrayList<Game> games = new ArrayList<>();
    private static ArrayList<Round> baseRounds = new ArrayList<>();

    private static Population makePopulation() {
        // Create the populations

        // Create rounds
        ArrayList<Round> rounds = new ArrayList<>();
        for (Round r : baseRounds) {
            rounds.add(r.clone());
        }


        // Add the games to a random round
        for (Game game : games) {
            Round round = rounds.get(Globals.randomGen.nextInt(rounds.size()));
            round.addGame(game);
        }
        return new Population(rounds);
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

        // Teema 1 has 12 * 2 = 24 rounds
        for (int j = 0; j < 24; j++) {
            baseRounds.add(new Round());
        }

        // Add bound games into baseRounds here
    }
}
