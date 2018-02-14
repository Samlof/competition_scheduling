package com.samuli;

import java.util.ArrayList;

public class Team {
    final int id;
    final String name;
    static ArrayList<Team> teams = new ArrayList<>();

    Team(String name) {
        this.id = teams.size();
        this.name = name;
        teams.add(this);
    }

    public boolean compare(Team other) {
        return id == other.id;
    }

    public static int[] getIntArray() {
        int array[] = new int[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            array[i] = 0;
        }
        return array;
    }

    public static double[] getDoubleArray() {
        double array[] = new double[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            array[i] = 0;
        }
        return array;
    }

    public static Team get(int i) {
        return teams.get(i);
    }

    public static int getTeamCount() {
        return teams.size();
    }
}
