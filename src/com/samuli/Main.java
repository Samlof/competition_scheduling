package com.samuli;


import java.io.PrintWriter;
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
                lowestPop = new Population(p.rounds);

                System.out.println("Roundnr: " + roundNr + " total error: " + populations[0].getTotalError());
                if (p.getTotalError() == 0) break;
            }
        }
        System.out.println("------------------End------------------");

        saveToFile(lowestPop);

        System.out.println("Result saved to file output.txt");
    }

    private static void saveToFile(Population p) {
        PrintWriter writer;
        try {

            writer = new PrintWriter("output.txt");
        } catch (java.io.FileNotFoundException e) {
            // Should not get here, as PrintWriter will create the file if not found
            System.out.println("Error! Stopping saving! File output.txt not found. Message: " + e.getLocalizedMessage());
            return;
        }
        // Eka rivi: kaikki_virheet hard_virheet soft_virheet (esim. 5 0 5)
        writer.print((p.getHardError() + p.getSoftError()) + " ");
        writer.print(p.getHardError() + " ");
        writer.println(p.getSoftError());

        // Toka rivi: pelatut_virheet kotiesto_virheet vierasesto_virheet break_virheet (esim. 0 0 0 5)
        writer.print(p.getTeamCountError() + " ");
        writer.print("0" + " ");
        writer.print("0" + " ");
        writer.println("0");

        // Loput rivit: kierros koti_joukkue vieras_joukkue lukittu_kierrokselle_ei_tai_joo (esim. 1 10 1 joo ja 3 9 6 ei)
        for (int i = 0; i < p.rounds.size(); i++) {
            Round r = p.rounds.get(i);
            for (Game g : r.games) {
                writer.println(i + " " + g.home.id + " " + g.guest.id + " " + (g.bound ? "joo" : "ei"));
            }
        }

        writer.close();
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
