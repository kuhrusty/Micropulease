package com.kuhrusty.micropul.model;

/**
 * The externally visible information you have about your opponent: how many
 * tiles they have in hand, but not what those tiles are, etc.  This gets
 * passed to bots instead of the opponent's Player instance, which would
 * include the actual tiles in hand.
 */
public class Opponent {

    public Opponent(Player player) {
        name = player.getName();
        type = player.getType();
    }

    /**
     * Returns the player's name, or null.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns some description of the player (bot type, or human), or null.
     */
    public String getType() {
        return type;
    }

    public int getTilesInHand() {
        return hand;
    }
    public int getTilesInSupply() {
        return supply;
    }
    public int getScore() {
        return score;
    }
    public int getMovesRemaining() {
        return moves;
    }

    private String name;
    private String type;
    private int hand;
    private int supply;
    private int score;
    private int moves;
}
