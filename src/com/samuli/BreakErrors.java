package com.samuli;

public class BreakErrors {

    private final int[] homeGameCounts;
    private final int[] awayGameCounts;
    private final int[] errorsByHomeTeam;
    private final int[] errorsByAwayTeam;

    private Round nextRound;
    private Round prevRound;

    public BreakErrors() {
        homeGameCounts = Team.getIntArray();
        awayGameCounts = Team.getIntArray();
        errorsByHomeTeam = Team.getIntArray();
        errorsByAwayTeam = Team.getIntArray();
    }


    public void setNextAndPrevRounds(Round next, Round previous) {
        nextRound = next;
        prevRound = previous;
    }

    public void setHomeError(Team t, int value) {
        errorsByHomeTeam[t.id] = value;
    }

    public void setAwayError(Team t, int value) {
        errorsByAwayTeam[t.id] = value;
    }
    public void addGame(Game game) {
        homeGameCounts[game.home.id]++;
        updateAway(game.home);
        updateHome(game.home);

        awayGameCounts[game.guest.id]++;
        updateAway(game.guest);
        updateHome(game.guest);
    }

    public void removeGame(Game game) {
        if (homeGameCounts[game.home.id] == 0) {
            System.out.println("BreakErrors::removeGame  Trying to remove a game that isn't in!");
        }
        homeGameCounts[game.home.id]--;
        updateAway(game.home);
        updateHome(game.home);


        if (awayGameCounts[game.guest.id] == 0) {
            System.out.println("BreakErrors::removeGame  Trying to remove a game that isn't in!");
        }
        awayGameCounts[game.guest.id]--;
        updateAway(game.guest);
        updateHome(game.guest);
    }

    public void updateHome(Team t) {
        Boolean searchingForHome = false;

        int gamesBefore = findGamesBefore(t, searchingForHome);
        int gamesAfter = findGamesAfter(t, searchingForHome);
        boolean thisRoundHasGame = hasHomeGame(t);

        // Handle all the different situations

        if (gamesAfter + gamesBefore == 0) {
            errorsByHomeTeam[t.id] = 0;
        }
        // This round has no game and before or after has only 1 games
        if (thisRoundHasGame == false && gamesAfter + gamesBefore == 1) {
            errorsByHomeTeam[t.id] = 0;
            // Update that one game's error to 0, since it doesn't have one anymore

            // Search it
            Round currRound = prevRound;
            while (currRound != null && currRound.getBreakErrorsClass().hasAwayGame(t)) {
                if (currRound.getBreakErrorsClass().hasHomeGame(t)) {
                    // Found it, so update and return out
                    currRound.getBreakErrorsClass().setHomeError(t, 0);
                    return;
                }
                currRound = currRound.getBreakErrorsClass().prevRound;
            }

            // Didn't find it behind, so search forward
            currRound = nextRound;
            while (currRound != null && currRound.getBreakErrorsClass().hasAwayGame(t)) {
                if (currRound.getBreakErrorsClass().hasHomeGame(t)) {
                    // Found it, so update and return out
                    currRound.getBreakErrorsClass().setHomeError(t, 0);
                    return;
                }
                currRound = currRound.getBreakErrorsClass().nextRound;
            }

            // Should never get here, so print and error
            System.out.println("BreakErrors::update trying to update for a team with nothing to update");
        }
        // This round has no game and before or after has more than a single
        else if (thisRoundHasGame == false && gamesAfter + gamesBefore > 1) {
            errorsByHomeTeam[t.id] = 0;
            // The games before and after already have 1 as error, so don't change them
        }
        // This round has a game, and there is 1 game before or after this
        else if (gamesAfter + gamesBefore == 1) {
            // Add break error to this and that
            errorsByHomeTeam[t.id] = 1;

            // Search it
            Round currRound = prevRound;
            while (currRound != null && currRound.getBreakErrorsClass().hasAwayGame(t)) {
                if (currRound.getBreakErrorsClass().hasHomeGame(t)) {
                    // Found it, so update and return out
                    currRound.getBreakErrorsClass().setHomeError(t, 1);
                    return;
                }
                currRound = currRound.getBreakErrorsClass().prevRound;
            }

            // Didn't find it behind, so search forward
            currRound = nextRound;
            while (currRound != null && currRound.getBreakErrorsClass().hasAwayGame(t)) {
                if (currRound.getBreakErrorsClass().hasHomeGame(t)) {
                    // Found it, so update and return out
                    currRound.getBreakErrorsClass().setHomeError(t, 1);
                    return;
                }
                currRound = currRound.getBreakErrorsClass().nextRound;
            }

            // Should never get here, so print and error
            System.out.println("BreakErrors::update trying to update for a team with nothing to update");
        }

        // This round has a game, and there is more than 1 game after this
        else if (gamesAfter + gamesBefore > 1) {
            errorsByHomeTeam[t.id] = 1;
        }
    }

    // Same function as above. Just home and away things flipped
    public void updateAway(Team t) {
        Boolean searchingForHome = true;

        int gamesBefore = findGamesBefore(t, searchingForHome);
        int gamesAfter = findGamesAfter(t, searchingForHome);
        boolean thisRoundHasGame = hasAwayGame(t);

        // Handle all the different situations

        if (gamesAfter + gamesBefore == 0) {
            errorsByAwayTeam[t.id] = 0;
        } else
            // This round has no game and before or after has only 1 games
            if (thisRoundHasGame == false && gamesAfter + gamesBefore == 1) {
                errorsByAwayTeam[t.id] = 0;
                // Update that one game's error to 0, since it doesn't have one anymore

                // Search it
                Round currRound = prevRound;
                while (currRound != null && currRound.getBreakErrorsClass().hasHomeGame(t)) {
                    if (currRound.getBreakErrorsClass().hasAwayGame(t)) {
                        // Found it, so update and return out
                        currRound.getBreakErrorsClass().setAwayError(t, 0);
                        return;
                    }
                    currRound = currRound.getBreakErrorsClass().prevRound;
                }

                // Didn't find it behind, so search forward
                currRound = nextRound;
                while (currRound != null && currRound.getBreakErrorsClass().hasHomeGame(t)) {
                    if (currRound.getBreakErrorsClass().hasAwayGame(t)) {
                        // Found it, so update and return out
                        currRound.getBreakErrorsClass().setAwayError(t, 0);
                        return;
                    }
                    currRound = currRound.getBreakErrorsClass().nextRound;
                }

                // Should never get here, so print and error
                System.out.println("BreakErrors::update trying to update for a team with nothing to update");
            }
            // This round has no game and before or after has more than a single
            else if (thisRoundHasGame == false && gamesAfter + gamesBefore > 1) {
                errorsByAwayTeam[t.id] = 0;
                // The games before and after already have 1 as error, so don't change them
            }
            // This round has a game, and there is 1 game before or after this
            else if (gamesAfter + gamesBefore == 1) {
                // Add break error to this and that
                errorsByAwayTeam[t.id] = 1;

                // Search it
                Round currRound = prevRound;
                while (currRound != null && currRound.getBreakErrorsClass().hasHomeGame(t)) {
                    if (currRound.getBreakErrorsClass().hasAwayGame(t)) {
                        // Found it, so update and return out
                        currRound.getBreakErrorsClass().setAwayError(t, 1);
                        return;
                    }
                    currRound = currRound.getBreakErrorsClass().prevRound;
                }

                // Didn't find it behind, so search forward
                currRound = nextRound;
                while (currRound != null && currRound.getBreakErrorsClass().hasHomeGame(t)) {
                    if (currRound.getBreakErrorsClass().hasAwayGame(t)) {
                        // Found it, so update and return out
                        currRound.getBreakErrorsClass().setAwayError(t, 1);
                        return;
                    }
                    currRound = currRound.getBreakErrorsClass().nextRound;
                }

                // Should never get here, so print and error
                System.out.println("BreakErrors::update trying to update for a team with nothing to update");
            }

            // This round has a game, and there is more than 1 game after this
            else if (gamesAfter + gamesBefore > 1) {
                // Just update this, as the others already have it set
                errorsByAwayTeam[t.id] = 1;
            }
    }

    private int findGamesBefore(Team t, boolean searchingForHome) {
        int gamesBefore = 0;
        Round currRound = prevRound;
        if (searchingForHome) {
            // Search the games before this for one and count how many there were
            while (currRound != null && currRound.getBreakErrorsClass().hasHomeGame(t)) {
                if (currRound.getBreakErrorsClass().hasAwayGame(t)) gamesBefore++;
                currRound = currRound.getBreakErrorsClass().prevRound;
            }
            return gamesBefore;
        } else {
            // Same thing but searching for other game

            // Search the games before this for one and count how many there were
            while (currRound != null && currRound.getBreakErrorsClass().hasAwayGame(t)) {
                if (currRound.getBreakErrorsClass().hasHomeGame(t)) gamesBefore++;
                currRound = currRound.getBreakErrorsClass().prevRound;
            }
            return gamesBefore;
        }
    }

    private int findGamesAfter(Team t, boolean searchingForHome) {
        int gamesAfter = 0;
        Round currRound = nextRound;
        if (searchingForHome) {
            // Search the games after this for one and count how many there were
            while (currRound != null && currRound.getBreakErrorsClass().hasHomeGame(t)) {
                if (currRound.getBreakErrorsClass().hasAwayGame(t)) gamesAfter++;
                currRound = currRound.getBreakErrorsClass().nextRound;
            }
            return gamesAfter;
        } else {
            // Same thing but searching for other game

            // Search the games after this for one and count how many there were
            while (currRound != null && currRound.getBreakErrorsClass().hasAwayGame(t)) {
                if (currRound.getBreakErrorsClass().hasHomeGame(t)) gamesAfter++;
                currRound = currRound.getBreakErrorsClass().nextRound;
            }
            return gamesAfter;
        }
    }

    public boolean hasHomeGame(Team t) {
        return homeGameCounts[t.id] > 0;
    }

    public boolean hasAwayGame(Team t) {
        return awayGameCounts[t.id] > 0;
    }
    public int getErrorByGame(Game g) {
        int error = 0;
        error += errorsByHomeTeam[g.home.id];
        error += errorsByAwayTeam[g.guest.id];
        return error;
    }

    public int getTotalErrors() {
        int error = 0;
        for (int i = 0; i < errorsByHomeTeam.length; i++) {
            error += errorsByHomeTeam[i];
            error += errorsByAwayTeam[i];
        }
        return error;
    }
}
