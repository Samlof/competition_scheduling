package com.samuli;


import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
                // Add 1 to ids, because my ids start from 0. Expected file starts from 1
                writer.println((i + 1) + " " + (g.home.id + 1) + " " + (g.guest.id + 1) + " " + (g.bound ? "joo" : "ei"));
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
        List<String> constraints;
        try {

            constraints = Files.readAllLines(Paths.get("Constaints.txt"));
        } catch (Exception e) {
            System.out.println("No Constaints.txt file found!");
            constraints = null;
            return;
        }
        if (constraints == null) return;

        // Read some options
        int index = 0;
        while (constraints.get(index).equals("#number of teams") == false) index++;
        int teamCounts = Integer.parseInt(constraints.get(index + 1));
        while (constraints.get(index).equals("#number of round robins (RR)") == false) index++;
        int RRCount = Integer.parseInt(constraints.get(index + 1));
        while (constraints.get(index).equals("#number of rounds") == false) index++;
        int roundCount = Integer.parseInt(constraints.get(index + 1));

        // Create teams
        while (constraints.get(index).equals("#team names") == false) index++;
        index++;
        for (int i = 0; i < teamCounts; i++) {
            Team a = new Team(constraints.get(index + i));
        }

        // Create games from teams for RR
        ArrayList<Team> teams = Team.teams;

        while (RRCount > 1) {
            for (Team team1 : teams) {
                for (Team team2 : teams) {
                    if (team1 == team2) continue;
                    games.add(new Game(team1, team2));
                }
            }
            RRCount -= 2;
        }
        // If there is one more round of games
        if (RRCount == 1) {
            // TODO: Not needed for Teema since it's always 2
        }

        // Create additional games
        while (constraints.get(index).equals("#additional games") == false) index++;
        index++;
        while (constraints.get(index).equals("#weekdays for rounds (1 = mon, 2 = tue, …) and 1 means a consecutive calendar day, 0 not") == false) {
            String[] teamIds = constraints.get(index).split(" ");
            int team1 = Integer.parseInt(teamIds[0]) - 1;
            int team2 = Integer.parseInt(teamIds[1]) - 1;
            // My id's are from 0, file id's from 1, so subtract 1
            games.add(new Game(Team.get(team1), Team.get(team2)));
        }

        // Create empty rounds
        for (int j = 0; j < roundCount; j++) {
            baseRounds.add(new Round());
        }

        // Move index to certain day limitations
        while (constraints.get(index).equals("#team cannot play at home on a certain day (team-number round-number)") == false)
            index++;
        index++;

        // Home day limitations
        while (constraints.get(index).equals("#team cannot play away on a certain day (team-number round-number)") == false) {

            index++;
        }
        index++;

        // Home day limitations
        while (constraints.get(index).equals("#game must be preassigned on certain round (team-number team-number round-number)") == false) {

            index++;
        }

        // Bound games
        index++;
        for (; index < constraints.size(); index++) {
            // Parse the id's from string
            String[] ids = constraints.get(index).split(" ");
            int team1 = Integer.parseInt(ids[0]) - 1;
            int team2 = Integer.parseInt(ids[1]) - 1;
            int round = Integer.parseInt(ids[1]) - 1;

            // Find the game from games list
            for (int i = 0; i < games.size(); i++) {
                Game g = games.get(i);
                if (g.home.id == team1 - 1 && g.guest.id == team2 - 1) {
                    // Add the game to the round and remove it from games list
                    baseRounds.get(round).addBoundGame(g);
                    games.remove(i);
                    break;
                }
            }
        }
    }
}
