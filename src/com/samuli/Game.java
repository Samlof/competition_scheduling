package com.samuli;

public class Game {
    public final Team home;
    public final Team guest;
    public final boolean bound;


    public Game(Team home, Team guest, boolean bound) {
        this.home = home;
        this.guest = guest;
        this.bound = bound;
    }

    public Game(Team home, Team guest) {
        this(home, guest, false);
    }

    public String description() {
        return home.name + " - " + guest.name;
    }
}
