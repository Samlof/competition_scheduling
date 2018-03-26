package com.samuli;

public class Game {
    public final Team home;
    public final Team guest;
    public boolean bound;


    public Game(Team home, Team guest) {
        this.home = home;
        this.guest = guest;
        this.bound = false;
    }

    public String description() {
        return home.id + " - " + guest.id + " - " + (bound ? "bound" : "not");
    }
}
