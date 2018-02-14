package com.samuli;

public class Game {
    public final Team home;
    public final Team guest;

    public Game(Team home, Team guest) {
        this.home = home;
        this.guest = guest;
    }

    public String description() {
        return home.name + " - " + guest.name;
    }
}
