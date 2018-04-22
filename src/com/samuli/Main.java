package com.samuli;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.nio.file.Files.readAllLines;

public class Main {

    public static void main(String[] args) {
        makeGamesAndRounds();

        Population[] populations = new Population[Constants.POPULATION_COUNT];
        for (int i = 0; i < populations.length; i++) {
            populations[i] = makePopulation();
        }

        int lowestError = Integer.MAX_VALUE;
        int lowestRound = 0;
        Population lowestPop = null;

        for (int roundNr = 0; roundNr < Constants.LOOP_AMOUNT; roundNr++) {
            for (Population p : populations) {
                p.startFromRandomDevelop();
                for (int betteringRounds = 0; betteringRounds < Constants.BETTERING_ROUNDS; betteringRounds++) {
                    // Move the worst scored game BETTERING_ROUNDS times
                    p.develop();
                }
            }
            Globals.sa.calcNewProb();

            // Sort the array to get easy access to best and worst populations
            //sort(populations);
            //int indexToMutate = populations.length - 1;
            int indexToMutate = Globals.randomGen.nextInt(populations.length);
            //populations[populations.length - 1] = populations[0].combine(populations[1]);
            for (int i = 0; i < Constants.MUTATION_TIMES; i++) {
                populations[indexToMutate].mutate();
            }

            // Find out if we have a new best solution
            for (Population p : populations) {
                if (p.getTotalError() < lowestError) {
                    lowestError = p.getTotalError();
                    lowestRound = roundNr;
                    lowestPop = p.clone();

                    System.out.println("------------------NEW------------------");
                    System.out.println("Round: " + roundNr + " Total: " + lowestError);
                    System.out.println("Soft: " + p.getSoftError() + " break: " + p.getBreakErrors());
                    System.out.println("Hard: " + p.getHardError() + " pelimäärä: " + p.getTeamCountError() + " kotipeli: " + p.getHomeErrors() + " vieraspeli: " + p.getAwayErrors());
                    System.out.println("---------------------------------------");

                    if (lowestError == 0) break;
                }


            }
            // Print the round number after every 5000
            if (roundNr % 5000 == 0 && roundNr != 0) {
                System.out.println("--- ROUND " + roundNr + " ---");
            }

            if (lowestError == 0) break;
        }
        System.out.println("------------------End------------------");
        System.out.println("Lowest at round: " + lowestRound + " with error: " + lowestPop.getTotalError() + " lowest error:" + lowestError);
        saveToFile(lowestPop);

        System.out.println("Result saved to file output.txt");
    }

    private static void sort(Population[] populations) {
        Arrays.sort(populations, new Comparator<Population>() {
            public int compare(Population o1, Population o2) {
                // Intentional: Reverse order for this demo
                return o2.getTotalError() - o1.getTotalError();
            }
        });
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
        writer.print(p.getHomeErrors() + " ");
        writer.print(p.getAwayErrors() + " ");
        writer.println(p.getBreakErrors());

        // Loput rivit: kierros koti_joukkue vieras_joukkue lukittu_kierrokselle_ei_tai_joo (esim. 1 10 1 joo ja 3 9 6 ei)
        for (int i = 0; i < p.rounds.size(); i++) {
            Round r = p.rounds.get(i);
            for (Game g : r.games) {
                // Add 1 to ids, because my ids start from 0. Expected file starts from 1
                writer.println((i + 1) + " " + (g.home.id + 1) + " " + (g.guest.id + 1) + " " + (g.bound ? "joo" : "ei"));
            }
            for (Game g : r.boundGames) {
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
        List<String> constraints = null;
        try {
            Charset charset = Charset.forName("ISO-8859-1");
            constraints = readAllLines(Paths.get("Constraints.txt"), charset);
        } catch (IOException e) {
            e.printStackTrace();
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
        // Loop thru the games and create them
        while (constraints.get(index).startsWith("#") == false) {
            String[] teamIds = constraints.get(index).split(" ");
            int team1 = Integer.parseInt(teamIds[0]) - 1;
            int team2 = Integer.parseInt(teamIds[1]) - 1;
            // My id's are from 0, file id's from 1, so subtract 1
            games.add(new Game(Team.get(team1), Team.get(team2)));

            index++;
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
            // Parse id's into Team and Round objects
            String[] teamIds = constraints.get(index).split(" ");
            int team1id = Integer.parseInt(teamIds[0]) - 1;
            int roundid = Integer.parseInt(teamIds[1]) - 1;
            Team team = Team.get(team1id);
            Round r = baseRounds.get(roundid);

            // Add the limit
            r.setHomeGameLimit(team);
            index++;
        }
        index++;

        // Away day limitations
        while (constraints.get(index).equals("#game must be preassigned on certain round (team-number team-number round-number)") == false) {
            // Parse id's into Team and Round objects
            String[] teamIds = constraints.get(index).split(" ");
            int team1id = Integer.parseInt(teamIds[0]) - 1;
            int roundid = Integer.parseInt(teamIds[1]) - 1;
            Team team = Team.get(team1id);
            Round r = baseRounds.get(roundid);

            // Add the limit
            r.setAwayGameLimit(team);
            index++;
        }

        // Bound games
        index++;
        for (; index < constraints.size(); index++) {
            // Parse the id's from string
            String[] ids = constraints.get(index).split(" ");
            int team1id = Integer.parseInt(ids[0]) - 1;
            int team2id = Integer.parseInt(ids[1]) - 1;
            int roundid = Integer.parseInt(ids[2]) - 1;
            Team team1 = teams.get(team1id);
            Team team2 = teams.get(team2id);
            Round r = baseRounds.get(roundid);

            // Find the game from games list
            for (int i = 0; i < games.size(); i++) {
                Game g = games.get(i);
                if (g.home == team1 && g.guest == team2) {
                    g.bound = true;
                    // Add the game to the round and remove it from games list
                    r.addBoundGame(g);
                    games.remove(i);
                    break;
                }
            }
        }
    }
}
