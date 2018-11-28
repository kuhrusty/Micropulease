package com.kuhrusty.micropul.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about a single player: their name, whether they're player 1 or
 * 2, the list of tiles in their hand, etc.
 */
public class Player {

    /**
     * @param owner should be Owner.P1 or Owner.P2.
     * @param name may be null, but you probably don't want it to be.
     */
    public Player(Owner owner, String name) {
        this.owner = owner;
        this.name = name;
        hand = new ArrayList<>(6);
        tokens = 3;
    }

    /**
     * Creates a deep copy of the given Player, which must not be null.
     */
    public Player(Player other) {
        owner = other.owner;
        name = other.name;
        type = other.type;
        hand = new ArrayList<Tile>(other.hand.size());
        for (Tile tt : other.hand) {
            hand.add(new Tile(tt));
        }
        supply = other.supply;
        tokens = other.tokens;
        score = other.score;
        moves = other.moves;
        hotseat = other.hotseat;
    }

    /**
     * Returns the player's name, or null.
     */
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns some description of the player (bot type, or human), or null.
     */
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getTilesInHand() {
        return hand.size();
    }
    public Tile getTile(int index) {
        return hand.get(index);
    }
    public List<Tile> getTiles() {
        return hand;
    }
    public void addToHand(Tile tile) {
        hand.add(tile);
    }
    public Tile removeTileByID(int id) {
        for (int ii = hand.size() - 1; ii >= 0; --ii) {
            if (hand.get(ii).getID() == id) {
                return hand.remove(ii);
            }
        }
        return null;
    }
    public int getTilesInSupply() {
        return supply;
    }
    public void setTilesInSupply(int supply) {
        this.supply = supply;
    }
    public int getTokensRemaining() {
        return tokens;
    }
    public void setTokensRemaining(int remaining) {
        tokens = remaining;
    }
    public int getScore() {
        return score;
    }
    public int getMovesRemaining() {
        return moves;
    }
    public boolean isHotSeat() {
        return hotseat;
    }
    public void setHotSeat(boolean hotseat) {
        this.hotseat = hotseat;
    }
    public Owner getOwner() {
        return owner;
    }

    private Owner owner;  //  bluhh, just whether they're P1 or P2.
    private String name;
    private String type;
    private List<Tile> hand;
    private int supply;
    private int tokens;
    private int score;
    private int moves;
    private boolean hotseat;
}
